package io.enderdev.linkedtanks.tiles

import com.azul.crs.client.Utils.uuid
import io.enderdev.linkedtanks.LinkedTanks
import io.enderdev.linkedtanks.Tags
import io.enderdev.linkedtanks.data.LTPersistentData
import io.enderdev.linkedtanks.data.LTPersistentData.DimBlockPos.Companion.dim
import io.enderdev.linkedtanks.data.LTPersistentData.dimId
import io.enderdev.linkedtanks.tiles.TileLinkedTank.Companion.NO_CHANNEL
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
import org.ender_development.catalyx.utils.extensions.readString
import org.ender_development.catalyx.utils.extensions.writeString
import sun.audio.AudioPlayer.player

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

	var updateTicks = 2

	override fun update() {
		markDirtyGUIEvery(7)
		if(++updateTicks == 3) {
			updateTicks = 0
			if((channelId != NO_CHANNEL && channelData == null) || channelData?.deleted == true)
				unlink()
		}
	}

	fun notifyBreak() =
		unlink()

	fun unlink() {
		if(channelId == NO_CHANNEL)
			return

		LTPersistentData.data.get(channelId)?.linkedPositions?.remove(pos dim world.dimId)

		channelId = NO_CHANNEL
		channelData = null
	}

	fun link(newChannelId: Int, ctx: MessageContext) {
		if(channelId == newChannelId)
			return

		val player = ctx.serverHandler.player

		// unlink
		if(newChannelId == NO_CHANNEL) {
			if(LTPersistentData.canEdit(channelData!!, player.uniqueID))
				unlink()
			return
		}

		val newChannelData = LTPersistentData.data.get(newChannelId)
		if(newChannelData == null || newChannelData.deleted) // sanity check + never allow connecting to deleted channels
			return

		if(!LTPersistentData.canEdit(newChannelData, player.uniqueID))
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
				val newChannelId = if(button.channelId == CREATE_NEW_CHANNEL)
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
					if(newName.length > LTPersistentData.CHANNEL_NAME_LENGTH_LIMIT)
						newName = newName.substring(0, LTPersistentData.CHANNEL_NAME_LENGTH_LIMIT)
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

		if(!tag.hasKey("OwnerUsername"))
			return

		val name = tag.getString("Name")
		val ownerUsername = tag.getString("OwnerUsername")
		val ownerUUID = tag.getUniqueId("OwnerUUID")!!
		val fluid = FluidRegistry.getFluid(tag.getString("FluidName"))
		val fluidAmount = tag.getInteger("FluidAmount")
		channelId = tag.getInteger("ChannelId")
		channelData = LTPersistentData.ChannelData(false, ownerUUID, ownerUsername, name, fluid, fluidAmount, NO_LINKED_POSITIONS).apply {
			fluidCapacityOverride = tag.getInteger("FluidCapacity")
		}
	}

	companion object {
		const val NO_CHANNEL = -1
		const val CREATE_NEW_CHANNEL = -101

		// client-side only, used in [handleUpdateTag] to try to avoid creating useless class instances
		val NO_LINKED_POSITIONS = HashSet<LTPersistentData.DimBlockPos>(0)
	}

	class LinkButtonWrapper(x: Int, y: Int) : AbstractButtonWrapper(x, y, 33, 13) {
		override val textureLocation = ResourceLocation(Tags.MOD_ID, "textures/gui/container/linked_tank_gui.png")

		override val drawDefaultHoverOverlay = false

		override val drawButton: () -> GuiButton.(Minecraft, Int, Int, Float) -> Unit = { { mc, mouseX, mouseY, partialTicks ->
			mc.textureManager.bindTexture(textureLocation)
			GlStateManager.color(1f, 1f, 1f)
			hovered = mouseX >= this.x && mouseX < this.x + width && mouseY >= this.y && mouseY < this.y + height
			drawTexturedModalRect(this.x, this.y, 193, if(hovered) 13 else 0, this.width, this.height)
		} }

		var channelId = NO_CHANNEL
		lateinit var ctx: MessageContext

		override fun readExtraData(buf: ByteBuf, ctx: MessageContext) {
			channelId = buf.readInt()
			this.ctx = ctx
		}

		override fun writeExtraData(buf: ByteBuf) {
			buf.writeInt(channelId)
		}
	}

	class RenameButtonWrapper(x: Int, y: Int) : AbstractButtonWrapper(x, y) {
		override val drawButton: () -> GuiButton.(Minecraft, Int, Int, Float) -> Unit = { { mc, mouseX, mouseY, partialTicks ->
		} }

		var newName = ""
		lateinit var ctx: MessageContext

		override fun readExtraData(buf: ByteBuf, ctx: MessageContext) {
			newName = buf.readString()
			this.ctx = ctx
		}

		override fun writeExtraData(buf: ByteBuf) {
			buf.writeString(newName)
		}
	}

	class DeleteButtonWrapper(x: Int, y: Int) : AbstractButtonWrapper(x, y, 36, 13) {
		override val textureLocation = ResourceLocation(Tags.MOD_ID, "textures/gui/container/linked_tank_gui.png")

		override val drawDefaultHoverOverlay = false

		override val drawButton: () -> GuiButton.(Minecraft, Int, Int, Float) -> Unit = { { mc, mouseX, mouseY, partialTicks ->
			mc.textureManager.bindTexture(textureLocation)
			GlStateManager.color(1f, 1f, 1f)
			hovered = mouseX >= this.x && mouseX < this.x + width && mouseY >= this.y && mouseY < this.y + height
			drawTexturedModalRect(this.x, this.y, 193, if(hovered) 39 else 26, this.width, this.height)
		} }

		lateinit var ctx: MessageContext

		override fun readExtraData(buf: ByteBuf, ctx: MessageContext) {
			this.ctx = ctx
		}
	}

	init {
		AbstractButtonWrapper.registerWrapper(LinkButtonWrapper::class.java)
		AbstractButtonWrapper.registerWrapper(RenameButtonWrapper::class.java)
		AbstractButtonWrapper.registerWrapper(DeleteButtonWrapper::class.java)
	}
}
