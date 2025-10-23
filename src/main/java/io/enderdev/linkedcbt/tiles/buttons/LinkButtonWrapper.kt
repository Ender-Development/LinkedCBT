package io.enderdev.linkedcbt.tiles.buttons

import io.enderdev.linkedcbt.client.gui.BaseLinkedGui
import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.base.client.BaseClientChannelListManager
import io.enderdev.linkedcbt.util.extensions.guiTranslate
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import org.ender_development.catalyx.client.button.AbstractButtonWrapper
import org.ender_development.catalyx.utils.extensions.readString
import org.ender_development.catalyx.utils.extensions.writeString

class LinkButtonWrapper : AbstractButtonWrapper {
	private lateinit var channelListManager: BaseClientChannelListManager<*, *, *>

	constructor(x: Int, y: Int, width: Int, height: Int, channelListManager: BaseClientChannelListManager<*, *, *>) : super(x, y, width, height) {
		this.channelListManager = channelListManager
	}

	@Deprecated("only used by Catalyx with reflection", level = DeprecationLevel.HIDDEN)
	constructor(x: Int, y: Int, width: Int, height: Int) : super(x, y, width, height)

	// TODO create an icon for the unlink part instead of drawing text
	override val textureLocation = Constants.LINKED_CBT_GUI

	override val drawDefaultHoverOverlay = false

	override val drawButton: () -> GuiButton.(Minecraft, Int, Int, Float) -> Unit = { { mc, mouseX, mouseY, partialTicks ->
		mc.textureManager.bindTexture(textureLocation)
		GlStateManager.color(1f, 1f, 1f)
		hovered = mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height

		if(channelId == Constants.NO_CHANNEL) {
			drawTexturedModalRect(this.x, this.y, BaseLinkedGui.UNLINK_BTN_U, if(hovered) BaseLinkedGui.UNLINK_BTN_V_HOVERED else BaseLinkedGui.UNLINK_BTN_V, this.width, this.height)
			BaseLinkedGui.FONT_RENDERER.drawString("unlink_btn".guiTranslate(), this.x + BaseLinkedGui.UNLINK_BTN_TEXT_OFF_X, this.y + BaseLinkedGui.UNLINK_BTN_TEXT_OFF_Y, BaseLinkedGui.TEXT_COLOUR)
		} else {
			drawTexturedModalRect(this.x, this.y, BaseLinkedGui.LINK_BTN_U, if(hovered) BaseLinkedGui.LINK_BTN_V_HOVERED else BaseLinkedGui.LINK_BTN_V, this.width, this.height)
			val channel = channelListManager.channels.find { it.id == channelId } ?: error("huh no channel with id $channelId in $channelListManager (ids: ${channelListManager.channels.map { it.id }})")
			BaseLinkedGui.FONT_RENDERER.drawString(
				channel.displayName,
				this.x + BaseLinkedGui.LINK_BTN_TEXT_OFF_X,
				this.y + BaseLinkedGui.LINK_BTN_TEXT_OFF_Y,
				BaseLinkedGui.TEXT_COLOUR
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
