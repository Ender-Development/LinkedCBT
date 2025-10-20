package io.enderdev.linkedcbt.tiles.buttons

import io.enderdev.linkedcbt.client.gui.GuiLinkedTank
import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.util.extensions.guiTranslate
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import org.ender_development.catalyx.client.button.AbstractButtonWrapper

class DeleteButtonWrapper(x: Int, y: Int) : AbstractButtonWrapper(x, y, GuiLinkedTank.DELETE_BTN_W, GuiLinkedTank.DELETE_BTN_H) {
	override val textureLocation = Constants.LINKED_TANK_GUI

	override val drawDefaultHoverOverlay = false

	override val drawButton: () -> GuiButton.(Minecraft, Int, Int, Float) -> Unit = { { mc, mouseX, mouseY, partialTicks ->
		mc.textureManager.bindTexture(textureLocation)
		GlStateManager.color(1f, 1f, 1f)
		hovered = mouseX >= this.x && mouseX < this.x + width && mouseY >= this.y && mouseY < this.y + height
		drawTexturedModalRect(this.x, this.y, GuiLinkedTank.DELETE_BTN_U, if(hovered) GuiLinkedTank.DELETE_BTN_V_HOVERED else GuiLinkedTank.DELETE_BTN_V, this.width, this.height)
		GuiLinkedTank.FONT_RENDERER.drawString("delete_btn".guiTranslate(), this.x + GuiLinkedTank.DELETE_BTN_TEXT_OFF_X, this.y + GuiLinkedTank.DELETE_BTN_TEXT_OFF_Y, GuiLinkedTank.RED_TEXT_COLOUR)
	} }

	lateinit var ctx: MessageContext

	override fun readExtraData(buf: ByteBuf, ctx: MessageContext) {
		this.ctx = ctx
	}
}
