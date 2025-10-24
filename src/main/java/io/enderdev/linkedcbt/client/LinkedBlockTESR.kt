package io.enderdev.linkedcbt.client

import io.enderdev.linkedcbt.tiles.BaseLinkedTile
import net.minecraft.client.renderer.BufferBuilder
import net.minecraftforge.client.model.animation.FastTESR

class LinkedBlockTESR : FastTESR<BaseLinkedTile<*, *, *, *>>() {
	override fun renderTileEntityFast(te: BaseLinkedTile<*, *, *, *>, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, partial: Float, buffer: BufferBuilder) {


		//GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f)
		//GlStateManager.enableBlend()
		//RenderHelper.enableStandardItemLighting()
		//GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
		//GlStateManager.pushMatrix()
		//val offset = sin((te.getWorld().getTotalWorldTime() - te.lastChangeTime + partialTicks) / 8) / 4.0
		//GlStateManager.translate(x + 0.5, y + 1.25 + offset, z + 0.5)
		//GlStateManager.rotate((te.getWorld().getTotalWorldTime() + partialTicks) * 4, 0f, 1f, 0f)
		//
		//Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
		//Minecraft.getMinecraft().getRenderItem().renderItem(stack, model)
		//
		//GlStateManager.popMatrix()
		//GlStateManager.disableRescaleNormal()
		//GlStateManager.disableBlend()



		//val size = 0.5
		//GlStateManager.pushMatrix()
		//GlStateManager.translate(x + 0.5, y + 1.01, z + 0.5)
		//buffer.pos(-size,  size, .0).tex(.0, .0).endVertex()
		//buffer.pos(size,  size, .0).tex(1.0, .0).endVertex()
		//buffer.pos(size, -size, .0).tex(1.0, 1.0).endVertex()
		//buffer.pos(-size, -size, .0).tex(.0, 1.0).endVertex()
		//GlStateManager.popMatrix()
	}
}
