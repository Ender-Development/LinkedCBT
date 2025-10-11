package io.enderdev.linkedtanks.tiles

import io.enderdev.linkedtanks.LinkedTanks
import io.enderdev.linkedtanks.Tags
import io.enderdev.linkedtanks.data.LTPersistentData
import io.enderdev.linkedtanks.data.LTPersistentData.DimBlockPos.Companion.dim
import io.enderdev.linkedtanks.data.LTPersistentData.dimId
import io.enderdev.linkedtanks.util.LinkedFluidHandler
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import org.ender_development.catalyx.client.button.AbstractButtonWrapper
import org.ender_development.catalyx.client.button.PauseButtonWrapper
import org.ender_development.catalyx.client.button.RedstoneButtonWrapper
import org.ender_development.catalyx.client.gui.BaseGuiTyped
import org.ender_development.catalyx.tiles.BaseTile
import org.ender_development.catalyx.tiles.helper.IButtonTile
import org.ender_development.catalyx.tiles.helper.IFluidTile
import org.ender_development.catalyx.tiles.helper.IGuiTile
import java.util.*

class TileLinkedTank : BaseTile(LinkedTanks.modSettings), IFluidTile, ITickable, IGuiTile, IButtonTile, BaseGuiTyped.IDefaultButtonVariables {
	override var isPaused = false
	override var needsRedstonePower = false
	// TODO
	var forcePush = false
	var forcePull = false

	var channelId = NO_CHANNEL

	var channelData: LTPersistentData.ChannelData? = LTPersistentData.data.get(channelId)
		set(value) {
			field = value
			fluidHandler.channelData = value
		}

	val fluidHandler = LinkedFluidHandler(null)

	override val fluidTanks = FluidHandlerConcatenate(fluidHandler)

	override fun update() {
		markDirtyGUIEvery(7)
	}

	init {
		if(channelId != NO_CHANNEL && !LTPersistentData.data.contains(channelId))
			unlink()
	}

	fun notifyBreak() {
		unlink()
	}

	fun unlink() {
		if(channelId == NO_CHANNEL)
			return

		LTPersistentData.data.get(channelId)?.linkedPositions?.remove(pos dim world.dimId)

		channelId = NO_CHANNEL
		channelData = null
	}

	fun link(newChannelId: Int, ctx: MessageContext) {
		// unlink
		if(newChannelId == NO_CHANNEL) {
			unlink()
			return
		}

		if(channelId == newChannelId)
			return

		val newChannelData = LTPersistentData.data.get(newChannelId)
		if(newChannelData == null) // sanity check
			return

		// permission check, maybe in the future this will be more elaborate ;p
		val player = ctx.serverHandler.player
		if(newChannelData.ownerUUID != player.uniqueID)
			return

		// update in case someone's changed their username
		newChannelData.ownerUsername = player.gameProfile.name

		unlink()

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
				if(button.channelId != NO_CHANNEL) {
					val newChannelId = if(button.channelId == CREATE_NEW_CHANNEL)
						LTPersistentData.createNewChannel(button.ctx!!.serverHandler.player, this)
					else
						button.channelId

					link(newChannelId, button.ctx!!)
				}
			}
		}
	}

	// server-side
	override fun readFromNBT(compound: NBTTagCompound) {
		if(compound.hasKey("ChannelId")) {
			channelId = compound.getInteger("ChannelId")
			channelData = LTPersistentData.data.get(channelId)
		}
		super.readFromNBT(compound)
	}

	// server-side
	override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
		super.writeToNBT(compound)
		if(channelId != NO_CHANNEL)
			compound.setInteger("ChannelId", channelId)
		return compound
	}

	// server-side, sent to client-side [handleUpdateTag]
	override fun getUpdateTag(): NBTTagCompound {
		return NBTTagCompound().apply {
			writeInternal(this) // write stuff like x,y,z + Forge stuff, since we don't call writeToNBT
			setInteger("ChannelId", channelId)
			channelData?.let { channelData ->
				setString("Name", channelData.name)
				setString("OwnerUsername", channelData.ownerUsername)
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

		if(!tag.hasKey("OwnerUsername"))
			return

		val name = tag.getString("Name")
		val ownerUsername = tag.getString("OwnerUsername")
		val fluid = FluidRegistry.getFluid(tag.getString("FluidName"))
		val fluidAmount = tag.getInteger("FluidAmount")
		channelId = tag.getInteger("ChannelId")
		channelData = LTPersistentData.ChannelData(NO_UUID, ownerUsername, name, fluid, fluidAmount, NO_LINKED_POSITIONS).apply {
			fluidCapacityOverride = tag.getInteger("FluidCapacity")
		}
	}

	companion object {
		const val NO_CHANNEL = -1
		const val CREATE_NEW_CHANNEL = -101

		// client-side only, used in [handleUpdateTag] to try to avoid creating useless class instances
		val NO_UUID: UUID = UUID.fromString("0-0-0-0-0")
		val NO_LINKED_POSITIONS = HashSet<LTPersistentData.DimBlockPos>(0)
	}

	class LinkButtonWrapper(x: Int, y: Int) : AbstractButtonWrapper(x, y, 32) {
		override val textureLocation = ResourceLocation(Tags.MOD_ID, "textures/gui/container/buttons.png")

		override val drawButton: () -> GuiButton.(Minecraft, Int, Int, Float) -> Unit = { { mc, mouseX, mouseY, partialTicks ->
			mc.textureManager.bindTexture(textureLocation)
			GlStateManager.color(1f, 1f, 1f)
			drawTexturedModalRect(this.x, this.y, 0, 0, 32, 16)
		} }

		var channelId = NO_CHANNEL
		var ctx: MessageContext? = null

		override fun readExtraData(buf: ByteBuf, ctx: MessageContext) {
			channelId = buf.readInt()
			this.ctx = ctx
		}

		override fun writeExtraData(buf: ByteBuf) {
			buf.writeInt(channelId)
		}
	}
}
