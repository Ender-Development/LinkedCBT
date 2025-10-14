package io.enderdev.linkedtanks.tiles.buttons

import io.enderdev.linkedtanks.client.gui.GuiLinkedTank
import io.enderdev.linkedtanks.data.Constants
import io.enderdev.linkedtanks.tiles.util.FluidSideConfiguration
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import org.ender_development.catalyx.client.button.AbstractButtonWrapper

class SideConfigurationButtonWrapper(x: Int, y: Int) : AbstractButtonWrapper(x, y, GuiLinkedTank.SIDE_CONFIG_BTN_W, GuiLinkedTank.SIDE_CONFIG_BTN_H) {
	override val textureLocation = Constants.LINKED_TANK_GUI

	override val drawButton: () -> GuiButton.(Minecraft, Int, Int, Float) -> Unit = { { mc, mouseX, mouseY, partialTicks ->
		mc.textureManager.bindTexture(textureLocation)
		GlStateManager.color(1f, 1f, 1f)
		drawTexturedModalRect(this.x, this.y, side.u, side.v, this.width, this.height)
	} }

	lateinit var facing: EnumFacing
	lateinit var side: FluidSideConfiguration.Side
	lateinit var ctx: MessageContext
	var affectsAll = false

	override fun readExtraData(buf: ByteBuf, ctx: MessageContext) {
		this.ctx = ctx
		facing = EnumFacing.byIndex(buf.readInt())
		side = FluidSideConfiguration.Side.entries[buf.readInt()]
		affectsAll = buf.readBoolean()
	}

	override fun writeExtraData(buf: ByteBuf) {
		buf.writeInt(facing.index)
		buf.writeInt(side.ordinal)
		buf.writeBoolean(affectsAll)
	}
}
