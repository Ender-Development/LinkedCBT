package io.enderdev.linkedtanks.tiles.buttons

import io.enderdev.linkedtanks.Tags
import io.enderdev.linkedtanks.data.Constants
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import org.ender_development.catalyx.client.button.AbstractButtonWrapper

class LinkButtonWrapper(x: Int, y: Int) : AbstractButtonWrapper(x, y, 33, 13) {
	override val textureLocation = ResourceLocation(Tags.MOD_ID, "textures/gui/container/linked_tank_gui.png")

	override val drawDefaultHoverOverlay = false

	override val drawButton: () -> GuiButton.(Minecraft, Int, Int, Float) -> Unit = { { mc, mouseX, mouseY, partialTicks ->
		mc.textureManager.bindTexture(textureLocation)
		GlStateManager.color(1f, 1f, 1f)
		hovered = mouseX >= this.x && mouseX < this.x + width && mouseY >= this.y && mouseY < this.y + height
		drawTexturedModalRect(this.x, this.y, 193, if(hovered) 13 else 0, this.width, this.height)
	} }

	var channelId = Constants.NO_CHANNEL
	lateinit var ctx: MessageContext

	override fun readExtraData(buf: ByteBuf, ctx: MessageContext) {
		channelId = buf.readInt()
		this.ctx = ctx
	}

	override fun writeExtraData(buf: ByteBuf) {
		buf.writeInt(channelId)
	}
}
