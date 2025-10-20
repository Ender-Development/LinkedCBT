package io.enderdev.linkedcbt.tiles.buttons

import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import org.ender_development.catalyx.client.button.AbstractButtonWrapper
import org.ender_development.catalyx.utils.extensions.readString
import org.ender_development.catalyx.utils.extensions.writeString

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
