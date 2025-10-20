package io.enderdev.linkedcbt.tiles.buttons

import io.enderdev.linkedcbt.data.tanks.client.ClientTankChannelListManager
import io.enderdev.linkedcbt.client.gui.GuiLinkedTank
import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.util.extensions.guiTranslate
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import org.ender_development.catalyx.client.button.AbstractButtonWrapper
import org.ender_development.catalyx.utils.extensions.readString
import org.ender_development.catalyx.utils.extensions.writeString

class LinkButtonWrapper(x: Int, y: Int, width: Int, height: Int) : AbstractButtonWrapper(x, y, width, height) {
	override val textureLocation = Constants.LINKED_TANK_GUI

	override val drawDefaultHoverOverlay = false

	override val drawButton: () -> GuiButton.(Minecraft, Int, Int, Float) -> Unit = { { mc, mouseX, mouseY, partialTicks ->
		mc.textureManager.bindTexture(textureLocation)
		GlStateManager.color(1f, 1f, 1f)
		hovered = mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height

		if(channelId == Constants.NO_CHANNEL) {
			drawTexturedModalRect(this.x, this.y, GuiLinkedTank.UNLINK_BTN_U, if(hovered) GuiLinkedTank.UNLINK_BTN_V_HOVERED else GuiLinkedTank.UNLINK_BTN_V, this.width, this.height)
			GuiLinkedTank.FONT_RENDERER.drawString("unlink_btn".guiTranslate(), this.x + GuiLinkedTank.UNLINK_BTN_TEXT_OFF_X, this.y + GuiLinkedTank.UNLINK_BTN_TEXT_OFF_Y, GuiLinkedTank.TEXT_COLOUR)
		} else {
			drawTexturedModalRect(this.x, this.y, GuiLinkedTank.LINK_BTN_U, if(hovered) GuiLinkedTank.LINK_BTN_V_HOVERED else GuiLinkedTank.LINK_BTN_V, this.width, this.height)
			val channel = if(channelId == Constants.CREATE_NEW_CHANNEL) Constants.CLIENT_CHANNEL_CREATE_NEW else ClientTankChannelListManager.channels.find { it.id == channelId }!!
			GuiLinkedTank.FONT_RENDERER.drawString(
				channel.displayName,
				this.x + GuiLinkedTank.LINK_BTN_TEXT_OFF_X,
				this.y + GuiLinkedTank.LINK_BTN_TEXT_OFF_Y,
				GuiLinkedTank.TEXT_COLOUR
			)
		}
	} }

	var channelId = Constants.NO_CHANNEL
	var newChannelNameOverride = ""
	lateinit var ctx: MessageContext

	override fun readExtraData(buf: ByteBuf, ctx: MessageContext) {
		channelId = buf.readInt()
		newChannelNameOverride = buf.readString()
		this.ctx = ctx
	}

	override fun writeExtraData(buf: ByteBuf) {
		buf.writeInt(channelId)
		buf.writeString(newChannelNameOverride)
	}
}
