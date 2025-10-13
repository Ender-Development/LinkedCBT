package io.enderdev.linkedtanks.tiles.buttons

import io.enderdev.linkedtanks.Tags
import io.enderdev.linkedtanks.client.gui.GuiLinkedTank
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import org.ender_development.catalyx.client.button.AbstractButtonWrapper

class DeleteButtonWrapper(x: Int, y: Int) : AbstractButtonWrapper(x, y, GuiLinkedTank.DELETE_BTN_W, GuiLinkedTank.DELETE_BTN_H) {
	override val textureLocation = ResourceLocation(Tags.MOD_ID, "textures/gui/container/linked_tank_gui.png")

	override val drawDefaultHoverOverlay = false

	override val drawButton: () -> GuiButton.(Minecraft, Int, Int, Float) -> Unit = { { mc, mouseX, mouseY, partialTicks ->
		mc.textureManager.bindTexture(textureLocation)
		GlStateManager.color(1f, 1f, 1f)
		hovered = mouseX >= this.x && mouseX < this.x + width && mouseY >= this.y && mouseY < this.y + height
		drawTexturedModalRect(this.x, this.y, GuiLinkedTank.DELETE_BTN_U, if(hovered) GuiLinkedTank.DELETE_BTN_V_HOVERED else GuiLinkedTank.DELETE_BTN_V, this.width, this.height)
		Minecraft.getMinecraft().fontRenderer.drawString("Delete", this.x + GuiLinkedTank.DELETE_BTN_TEXT_OFF_X, this.y + GuiLinkedTank.DELETE_BTN_TEXT_OFF_Y, GuiLinkedTank.RED_TEXT_COLOUR)
	} }

	lateinit var ctx: MessageContext

	override fun readExtraData(buf: ByteBuf, ctx: MessageContext) {
		this.ctx = ctx
	}
}
