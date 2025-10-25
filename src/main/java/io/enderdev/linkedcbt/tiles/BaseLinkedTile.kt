package io.enderdev.linkedcbt.tiles

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.base.BaseChannelData
import io.enderdev.linkedcbt.data.base.BasePersistentData
import io.enderdev.linkedcbt.tiles.buttons.DeleteButtonWrapper
import io.enderdev.linkedcbt.tiles.buttons.LinkButtonWrapper
import io.enderdev.linkedcbt.tiles.buttons.RenameButtonWrapper
import io.enderdev.linkedcbt.tiles.buttons.SideConfigurationButtonWrapper
import io.enderdev.linkedcbt.tiles.util.BaseSideConfiguration
import io.enderdev.linkedcbt.util.BaseLinkedHandler
import io.enderdev.linkedcbt.util.extensions.dim
import io.enderdev.linkedcbt.util.extensions.dimId
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.common.capabilities.Capability
import org.ender_development.catalyx.client.button.AbstractButtonWrapper
import org.ender_development.catalyx.client.button.PauseButtonWrapper
import org.ender_development.catalyx.client.button.RedstoneButtonWrapper
import org.ender_development.catalyx.client.gui.BaseGuiTyped
import org.ender_development.catalyx.tiles.BaseTile
import org.ender_development.catalyx.tiles.helper.IButtonTile
import org.ender_development.catalyx.tiles.helper.ICopyPasteExtraTile
import org.ender_development.catalyx.tiles.helper.IGuiTile
import java.util.*

abstract class BaseLinkedTile<TE : BaseLinkedTile<TE, CH_DATA, CAP_TYPE, LINKED_HANDLER>, CH_DATA : BaseChannelData<CH_DATA, *>, CAP_TYPE : Any, LINKED_HANDLER : BaseLinkedHandler<CAP_TYPE, CH_DATA>>(val persistentData: BasePersistentData<CH_DATA, TE>, val capType: Capability<CAP_TYPE>) : BaseTile(LinkedCBT), ITickable, IGuiTile, IButtonTile, BaseGuiTyped.IDefaultButtonVariables, ICopyPasteExtraTile {
	override var isPaused = false
	override var needsRedstonePower = false

	var channelId = Constants.NO_CHANNEL
	var channelData: CH_DATA? = persistentData.data.get(channelId)
		set(value) {
			field = value
			linkedHandler.channelData = value
		}

	abstract val sideConfiguration: BaseSideConfiguration<CAP_TYPE>
	abstract val linkedHandler: LINKED_HANDLER

	private var channelUpdateTicks = 2
	private var channelUnfuckTicks = 10
	override fun update() {
		// don't tick on client-side or when we don't have any channel
		if(world.isRemote || channelId == Constants.NO_CHANNEL)
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
						LinkedCBT.logger.debug("Channel id {} didn't have us ({}) added to linked positions! This shouldn't happen!", channelId, pos)
				}
			}
		}
		sideConfiguration.tick()
	}

	fun notifyBreak() =
		unlink()

	fun unlink() {
		if(channelId == Constants.NO_CHANNEL)
			return

		persistentData.data.get(channelId)?.linkedPositions?.remove(pos dim world.dimId)

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

		val newChannelData = persistentData.data.get(newChannelId)
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

		val newChannelData = persistentData.data.get(newChannelId)
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
				@Suppress("UNCHECKED_CAST") // the cast warning at the time of writing is 1062 characters long, thanks IntelliJ
				val newChannelId = if(button.channelId == Constants.CREATE_NEW_CHANNEL)
					persistentData.createNewChannel(player, this as TE, transformChannelName(button.newChannelNameOverride))
				else
					button.channelId

				link(newChannelId, player)
			}
			is RenameButtonWrapper -> {
				channelData?.let { channelData ->
					if(!channelData.canBeEditedBy(button.ctx.serverHandler.player.uniqueID))
						return

					val newName = transformChannelName(button.newName) ?: return

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
					sideConfiguration.sides.replaceAll { _, _ -> button.side }
				else
					sideConfiguration.sides[button.facing] = button.side

				markDirtyGUI()
			}
		}
	}

	fun transformChannelName(name: String): String? {
		var name = name.trim()
		if(name.length > Constants.CHANNEL_NAME_LENGTH_LIMIT)
			name = name.substring(0, Constants.CHANNEL_NAME_LENGTH_LIMIT).trim()

		return name.ifEmpty { null }
	}

	// server-side
	override fun readFromNBT(compound: NBTTagCompound) {
		if(compound.hasKey("ChannelId"))
			link(compound.getInteger("ChannelId"))

		sideConfiguration.readFromNBT(compound.getCompoundTag("SideConfiguration"))
		super.readFromNBT(compound)
	}

	// server-side
	override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
		super.writeToNBT(compound)

		// these might mess with our TEs and cause duplication
		compound.removeTag("EnergyStored")
		compound.removeTag("input")
		compound.removeTag("output")

		if(channelId != Constants.NO_CHANNEL)
			compound.setInteger("ChannelId", channelId)

		compound.setTag("SideConfiguration", sideConfiguration.writeToNBT(false))

		return compound
	}

	override fun getUpdatePacket() =
		SPacketUpdateTileEntity(pos, 0, updateTag)

	// server-side, sent to client-side [handleUpdateTag]
	override fun getUpdateTag(): NBTTagCompound {
		return NBTTagCompound().apply {
			writeInternal(this) // write stuff like x,y,z + Forge stuff, since we don't call writeToNBT
			setTag("SideConfiguration", sideConfiguration.writeToNBT(true))
			setInteger("ChannelId", channelId)

			channelData?.let { channelData ->
				setString("Name", channelData.name)
				setString("OwnerUsername", channelData.ownerUsername)
				setUniqueId("OwnerUUID", channelData.ownerUUID)

				writeClientChannelData(channelData, this)
			}
		}
	}

	abstract fun writeClientChannelData(channelData: CH_DATA, tag: NBTTagCompound)

	override fun onDataPacket(net: NetworkManager, pkt: SPacketUpdateTileEntity) {
		handleUpdateTag(pkt.nbtCompound)
	}

	// client-side, handle from server-side [getUpdateTag]
	override fun handleUpdateTag(tag: NBTTagCompound) {
		super.readFromNBT(tag)

		sideConfiguration.readFromNBT(tag.getCompoundTag("SideConfiguration"))
		channelId = tag.getInteger("ChannelId")

		if(!tag.hasKey("OwnerUsername")) {
			channelData = null
			return
		}

		val name = tag.getString("Name")
		val ownerUsername = tag.getString("OwnerUsername")
		val ownerUUID = tag.getUniqueId("OwnerUUID")!!
		channelData = readClientChannelData(tag, name, ownerUsername, ownerUUID)
	}

	abstract fun readClientChannelData(tag: NBTTagCompound, name: String, ownerUsername: String, ownerUUID: UUID): CH_DATA

	override fun hasCapability(capability: Capability<*>, facing: EnumFacing?) =
		capability == capType && sideConfiguration.hasCapability(facing)

	override fun <T : Any> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
		return if(capability == capType)
			capType.cast(sideConfiguration.getCapability(facing) ?: return null)
		else
			null
	}

	// ICopyPasteExtraTile
	override fun copyData(tag: NBTTagCompound) {
		tag.setInteger("ChannelId", channelId)
		tag.setTag("SideConfiguration", sideConfiguration.writeToNBT(true))
	}

	override fun pasteData(tag: NBTTagCompound, player: EntityPlayer) {
		if(channelData?.canBeEditedBy(player.uniqueID) == false)
			return

		if(tag.hasKey("ChannelId")) {
			val channelId = tag.getInteger("ChannelId")
			if(channelId != Constants.CREATE_NEW_CHANNEL) // sanity check
				link(channelId, player)
		}

		if(tag.hasKey("SideConfiguration"))
			sideConfiguration.readFromNBT(tag.getCompoundTag("SideConfiguration"))
	}

	init {
		AbstractButtonWrapper.registerWrapper(LinkButtonWrapper::class.java)
		AbstractButtonWrapper.registerWrapper(RenameButtonWrapper::class.java)
		AbstractButtonWrapper.registerWrapper(DeleteButtonWrapper::class.java)
		AbstractButtonWrapper.registerWrapper(SideConfigurationButtonWrapper::class.java)
	}
}
