package io.enderdev.linkedtanks.tiles

import io.enderdev.linkedtanks.LinkedTanks
import io.enderdev.linkedtanks.data.ChannelData
import io.enderdev.linkedtanks.data.Constants
import io.enderdev.linkedtanks.data.LTPersistentData
import io.enderdev.linkedtanks.tiles.buttons.DeleteButtonWrapper
import io.enderdev.linkedtanks.tiles.buttons.LinkButtonWrapper
import io.enderdev.linkedtanks.tiles.buttons.RenameButtonWrapper
import io.enderdev.linkedtanks.tiles.util.FluidSideConfiguration
import io.enderdev.linkedtanks.util.LinkedFluidHandler
import io.enderdev.linkedtanks.util.extensions.dim
import io.enderdev.linkedtanks.util.extensions.dimId
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import org.ender_development.catalyx.client.button.AbstractButtonWrapper
import org.ender_development.catalyx.client.button.PauseButtonWrapper
import org.ender_development.catalyx.client.button.RedstoneButtonWrapper
import org.ender_development.catalyx.client.gui.BaseGuiTyped
import org.ender_development.catalyx.tiles.BaseTile
import org.ender_development.catalyx.tiles.helper.IButtonTile
import org.ender_development.catalyx.tiles.helper.IFluidTile
import org.ender_development.catalyx.tiles.helper.IGuiTile

class TileLinkedTank : BaseTile(LinkedTanks.modSettings), IFluidTile, ITickable, IGuiTile, IButtonTile, BaseGuiTyped.IDefaultButtonVariables {
	override var isPaused = false
	override var needsRedstonePower = false

	var channelId = Constants.NO_CHANNEL
	var channelData: ChannelData? = LTPersistentData.data.get(channelId)
		set(value) {
			field = value
			fluidHandler.channelData = value
		}

	val fluidSideConfiguration = FluidSideConfiguration(this)
	override val fluidHandler = LinkedFluidHandler(null)

	var channelUpdateTicks = 2
	override fun update() {
		markDirtyGUIEvery(7)
		if(++channelUpdateTicks == 3) {
			channelUpdateTicks = 0
			if((channelId != Constants.NO_CHANNEL && channelData == null) || channelData?.deleted == true)
				unlink()
		}
		fluidSideConfiguration.tick()
	}

	fun notifyBreak() =
		unlink()

	fun unlink() {
		if(channelId == Constants.NO_CHANNEL)
			return

		LTPersistentData.data.get(channelId)?.linkedPositions?.remove(pos dim world.dimId)

		channelId = Constants.NO_CHANNEL
		channelData = null
	}

	fun link(newChannelId: Int, ctx: MessageContext) {
		if(channelId == newChannelId)
			return

		val player = ctx.serverHandler.player

		// unlink
		if(newChannelId == Constants.NO_CHANNEL) {
			if(channelData!!.canBeEditedBy(player.uniqueID))
				unlink()
			return
		}

		val newChannelData = LTPersistentData.data.get(newChannelId)
		if(newChannelData == null || newChannelData.deleted) // sanity check + never allow connecting to deleted channels
			return

		if(!newChannelData.canBeEditedBy(player.uniqueID))
			return

		// update in case someone's changed their username
		newChannelData.ownerUsername = player.gameProfile.name

		unlink()

		newChannelData.linkedPositions.add(pos dim world.dimId)
		channelId = newChannelId
		channelData = newChannelData

		markDirtyGUI()
	}

	fun link(newChannelId: Int) {
		if(channelId == newChannelId)
			return

		val newChannelData = LTPersistentData.data.get(newChannelId)
		if(newChannelData == null || newChannelData.deleted) // sanity check + never allow connecting to deleted channels
			return

		unlink()

		// unlink
		if(newChannelId == Constants.NO_CHANNEL)
			return

		newChannelData.linkedPositions.add(pos dim world.dimId)
		channelId = newChannelId
		channelData = newChannelData

		markDirtyGUI()
	}

	override fun handleButtonPress(button: AbstractButtonWrapper) {
		when(button) {
			is PauseButtonWrapper -> isPaused = !isPaused
			is RedstoneButtonWrapper -> needsRedstonePower = !needsRedstonePower
			is LinkButtonWrapper -> {
				val newChannelId = if(button.channelId == Constants.CREATE_NEW_CHANNEL)
					LTPersistentData.createNewChannel(button.ctx.serverHandler.player, this)
				else
					button.channelId

				link(newChannelId, button.ctx)
			}
			is RenameButtonWrapper -> {
				channelData?.let { channelData ->
					if(button.ctx.serverHandler.player.uniqueID != channelData.ownerUUID)
						return

					var newName = button.newName.trim()
					if(newName.length > Constants.CHANNEL_NAME_LENGTH_LIMIT)
						newName = newName.substring(0, Constants.CHANNEL_NAME_LENGTH_LIMIT)
					newName = newName.trim()
					if(newName.isEmpty())
						return

					channelData.name = newName
					markDirtyGUI()
				}
			}
			is DeleteButtonWrapper -> {
				channelData?.let { channelData ->
					if(button.ctx.serverHandler.player.uniqueID != channelData.ownerUUID)
						return

					channelData.deleted = true
					unlink()
					markDirtyGUI()
				}
			}
		}
	}

	// server-side
	override fun readFromNBT(compound: NBTTagCompound) {
		if(compound.hasKey("ChannelId"))
			link(compound.getInteger("ChannelId"))
		fluidSideConfiguration.readFromNBT(compound.getCompoundTag("FluidSideConfiguration"))
		super.readFromNBT(compound)
	}

	// server-side
	override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
		super.writeToNBT(compound)
		if(channelId != Constants.NO_CHANNEL)
			compound.setInteger("ChannelId", channelId)
		compound.setTag("FluidSideConfiguration", fluidSideConfiguration.writeToNBT())
		return compound
	}

	// server-side, sent to client-side [handleUpdateTag]
	override fun getUpdateTag(): NBTTagCompound {
		return NBTTagCompound().apply {
			writeInternal(this) // write stuff like x,y,z + Forge stuff, since we don't call writeToNBT
			setTag("FluidSideConfiguration", fluidSideConfiguration.writeToNBT())
			setInteger("ChannelId", channelId)
			channelData?.let { channelData ->
				setString("Name", channelData.name)
				setString("OwnerUsername", channelData.ownerUsername)
				setUniqueId("OwnerUUID", channelData.ownerUUID)
				if(channelData.fluid != null)
					setString("FluidName", FluidRegistry.getFluidName(channelData.fluid))
				setInteger("FluidAmount", channelData.fluidAmount)
				setInteger("FluidCapacity", channelData.fluidCapacity)
			}
		}
	}

	// client-side, handle from server-side [getUpdateTag]
	override fun handleUpdateTag(tag: NBTTagCompound) {
		super.readFromNBT(tag)

		fluidSideConfiguration.readFromNBT(tag.getCompoundTag("FluidSideConfiguration"))

		if(!tag.hasKey("OwnerUsername"))
			return

		val name = tag.getString("Name")
		val ownerUsername = tag.getString("OwnerUsername")
		val ownerUUID = tag.getUniqueId("OwnerUUID")!!
		val fluid = FluidRegistry.getFluid(tag.getString("FluidName"))
		val fluidAmount = tag.getInteger("FluidAmount")
		channelId = tag.getInteger("ChannelId")
		channelData = ChannelData(false, ownerUUID, ownerUsername, name, fluid, fluidAmount, Constants.NO_LINKED_POSITIONS).apply {
			fluidCapacityOverride = tag.getInteger("FluidCapacity")
		}
	}

	override fun hasCapability(capability: Capability<*>, facing: EnumFacing?) =
		capability == FLUID_CAP && fluidSideConfiguration.hasCapability(facing)

	override fun <T : Any> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
		return if(capability == FLUID_CAP)
			FLUID_CAP.cast(fluidSideConfiguration.getCapability(facing) ?: return null)
		else
			null
	}

	init {
		AbstractButtonWrapper.registerWrapper(LinkButtonWrapper::class.java)
		AbstractButtonWrapper.registerWrapper(RenameButtonWrapper::class.java)
		AbstractButtonWrapper.registerWrapper(DeleteButtonWrapper::class.java)
	}
}
