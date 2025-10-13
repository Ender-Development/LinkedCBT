package io.enderdev.linkedtanks.tiles

import io.enderdev.linkedtanks.LinkedTanks
import io.enderdev.linkedtanks.data.ChannelData
import io.enderdev.linkedtanks.data.Constants
import io.enderdev.linkedtanks.data.LTPersistentData
import io.enderdev.linkedtanks.tiles.buttons.DeleteButtonWrapper
import io.enderdev.linkedtanks.tiles.buttons.LinkButtonWrapper
import io.enderdev.linkedtanks.tiles.buttons.RenameButtonWrapper
import io.enderdev.linkedtanks.tiles.buttons.SideConfigurationButtonWrapper
import io.enderdev.linkedtanks.tiles.util.FluidSideConfiguration
import io.enderdev.linkedtanks.util.LinkedFluidHandler
import io.enderdev.linkedtanks.util.extensions.dim
import io.enderdev.linkedtanks.util.extensions.dimId
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.FluidRegistry
import org.ender_development.catalyx.client.button.AbstractButtonWrapper
import org.ender_development.catalyx.client.button.PauseButtonWrapper
import org.ender_development.catalyx.client.button.RedstoneButtonWrapper
import org.ender_development.catalyx.client.gui.BaseGuiTyped
import org.ender_development.catalyx.tiles.BaseTile
import org.ender_development.catalyx.tiles.helper.IButtonTile
import org.ender_development.catalyx.tiles.helper.ICopyPasteExtraTile
import org.ender_development.catalyx.tiles.helper.IFluidTile
import org.ender_development.catalyx.tiles.helper.IGuiTile

class TileLinkedTank : BaseTile(LinkedTanks.modSettings), IFluidTile, ITickable, IGuiTile, IButtonTile, BaseGuiTyped.IDefaultButtonVariables, ICopyPasteExtraTile {
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
	var channelUnfuckTicks = 10
	override fun update() {
		// don't tick on client-side
		if(world.isRemote)
			return

		markDirtyGUIEvery(5)
		if(++channelUpdateTicks == 3) {
			channelUpdateTicks = 0
			if((channelId != Constants.NO_CHANNEL && channelData == null) || channelData?.deleted == true)
				unlink()

			// this shouldn't happen but might as well
			if(++channelUnfuckTicks == 20) {
				channelUnfuckTicks = 0
				channelData?.let {
					val pos = pos dim world.dimId
					if(it.linkedPositions.add(pos))
						LinkedTanks.logger.debug("Channel id {} didn't have us ({}) added to linked positions! This shouldn't happen!", channelId, pos)
				}
			}
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

	fun link(newChannelId: Int, player: EntityPlayer) {
		if(channelId == newChannelId)
			return

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

		if(world != null) // in early readFromNBT, world is still null
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
				val player = button.ctx.serverHandler.player
				val newChannelId = if(button.channelId == Constants.CREATE_NEW_CHANNEL)
					LTPersistentData.createNewChannel(player, this)
				else
					button.channelId

				link(newChannelId, player)
			}
			is RenameButtonWrapper -> {
				channelData?.let { channelData ->
					if(!channelData.canBeEditedBy(button.ctx.serverHandler.player.uniqueID))
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
					if(!channelData.canBeEditedBy(button.ctx.serverHandler.player.uniqueID))
						return

					channelData.deleted = true
					unlink()
					markDirtyGUI()
				}
			}
			is SideConfigurationButtonWrapper -> {
				if(channelData?.canBeEditedBy(button.ctx.serverHandler.player.uniqueID) == false)
					return

				if(button.affectsAll)
					fluidSideConfiguration.sides.replaceAll { _, _ -> button.side }
				else
					fluidSideConfiguration.sides[button.facing] = button.side

				markDirtyGUI()
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

		compound.setTag("FluidSideConfiguration", fluidSideConfiguration.writeToNBT(false))

		return compound
	}

	override fun getUpdatePacket() =
		SPacketUpdateTileEntity(pos, 0, updateTag)

	// server-side, sent to client-side [handleUpdateTag]
	override fun getUpdateTag(): NBTTagCompound {
		return NBTTagCompound().apply {
			writeInternal(this) // write stuff like x,y,z + Forge stuff, since we don't call writeToNBT
			setTag("FluidSideConfiguration", fluidSideConfiguration.writeToNBT(true))
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

	override fun onDataPacket(net: NetworkManager, pkt: SPacketUpdateTileEntity) {
		handleUpdateTag(pkt.nbtCompound)
	}

	// client-side, handle from server-side [getUpdateTag]
	override fun handleUpdateTag(tag: NBTTagCompound) {
		super.readFromNBT(tag)

		fluidSideConfiguration.readFromNBT(tag.getCompoundTag("FluidSideConfiguration"))
		channelId = tag.getInteger("ChannelId")

		if(!tag.hasKey("OwnerUsername")) {
			channelData = null
			return
		}

		val name = tag.getString("Name")
		val ownerUsername = tag.getString("OwnerUsername")
		val ownerUUID = tag.getUniqueId("OwnerUUID")!!
		val fluid = FluidRegistry.getFluid(tag.getString("FluidName"))
		val fluidAmount = tag.getInteger("FluidAmount")
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

	// ICopyPasteExtraTile
	override fun copyData(tag: NBTTagCompound) {
		tag.setInteger("ChannelId", channelId)
		tag.setTag("FluidSideConfiguration", fluidSideConfiguration.writeToNBT(true))
	}

	override fun pasteData(tag: NBTTagCompound, player: EntityPlayer) {
		if(channelData?.canBeEditedBy(player.uniqueID) == false)
			return

		if(tag.hasKey("ChannelId")) {
			val channelId = tag.getInteger("ChannelId")
			if(channelId != Constants.CREATE_NEW_CHANNEL) // sanity check
				link(channelId, player)
		}

		if(tag.hasKey("FluidSideConfiguration"))
			fluidSideConfiguration.readFromNBT(tag.getCompoundTag("FluidSideConfiguration"))
	}

	init {
		AbstractButtonWrapper.registerWrapper(LinkButtonWrapper::class.java)
		AbstractButtonWrapper.registerWrapper(RenameButtonWrapper::class.java)
		AbstractButtonWrapper.registerWrapper(DeleteButtonWrapper::class.java)
		AbstractButtonWrapper.registerWrapper(SideConfigurationButtonWrapper::class.java)
	}
}
