package io.enderdev.linkedcbt.tiles.buttons

import io.enderdev.linkedcbt.client.gui.BaseLinkedGui
import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.tiles.util.SideConfiguration
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import org.ender_development.catalyx.client.button.AbstractButtonWrapper

class SideConfigurationButtonWrapper(x: Int, y: Int) : AbstractButtonWrapper(x, y, BaseLinkedGui.SIDE_CONFIG_BTN_W, BaseLinkedGui.SIDE_CONFIG_BTN_H) {
	override val textureLocation = Constants.LINKED_CBT_GUI

	override val drawButton: () -> GuiButton.(Minecraft, Int, Int, Float) -> Unit = { { mc, mouseX, mouseY, partialTicks ->
		mc.textureManager.bindTexture(textureLocation)
		GlStateManager.color(1f, 1f, 1f)
		drawTexturedModalRect(this.x, this.y, side.u, side.v, this.width, this.height)
	} }

	lateinit var facing: EnumFacing
	lateinit var side: SideConfiguration
	lateinit var ctx: MessageContext
	var affectsAll = false

	override fun readExtraData(buf: ByteBuf, ctx: MessageContext) {
		this.ctx = ctx
		facing = EnumFacing.byIndex(buf.readInt())
		side = SideConfiguration.entries[buf.readInt()]
		affectsAll = buf.readBoolean()
	}

	override fun writeExtraData(buf: ByteBuf) {
		buf.writeInt(facing.index)
		buf.writeInt(side.ordinal)
		buf.writeBoolean(affectsAll)
	}
}
