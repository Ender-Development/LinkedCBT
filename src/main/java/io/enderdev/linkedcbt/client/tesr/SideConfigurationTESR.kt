package io.enderdev.linkedcbt.client.tesr

import io.enderdev.linkedcbt.Tags
import io.enderdev.linkedcbt.tiles.BaseLinkedTile
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import org.ender_development.catalyx.client.tesr.AbstractTESRenderer
import org.ender_development.catalyx.tiles.BaseTile
import org.ender_development.catalyx.utils.RenderUtils
import org.ender_development.catalyx.utils.extensions.glOffsetX
import org.ender_development.catalyx.utils.extensions.glOffsetZ
import org.ender_development.catalyx.utils.extensions.glRotate
import org.ender_development.catalyx.utils.extensions.glRotationAngle
import org.lwjgl.opengl.GL11

internal object SideConfigurationTESR : AbstractTESRenderer() {
	override fun render(te: BaseTile, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) {
		if(te !is BaseLinkedTile<*, *, *, *>)
			error("how")

		te.sideConfiguration.sides.forEach { (side, state) ->
			val texture = ResourceLocation(Tags.MOD_ID, "textures/blocks/io/${state.name.lowercase()}.png")

			GlStateManager.pushMatrix()

			translateToSide(side, x, y, z)
			renderTexture(texture)

			GlStateManager.popMatrix()
		}
	}

	private fun renderTexture(texture: ResourceLocation) {
		GlStateManager.shadeModel(if(Minecraft.isAmbientOcclusionEnabled()) GL11.GL_SMOOTH else GL11.GL_FLAT)

		GlStateManager.translate(-.5, .0, .01)
		GlStateManager.scale(TESR_MAGIC_NUMBER, -TESR_MAGIC_NUMBER, TESR_MAGIC_NUMBER)
		GlStateManager.color(1f, 1f, 1f, 1f)

		RenderUtils.bindTexture(texture)
		RenderUtils.drawScaledCustomSizeModalRect(.0, .0, .0, .0, 16.0, 16.0, ONE_BLOCK_WIDTH, ONE_BLOCK_WIDTH, 16.0, 16.0)
	}

	/**
	 * Translates and rotates the GL matrix to render on the given side of a block at the given coordinates.
	 * Note: with this Implementation, the texture on side === DOWN is technically flipped top to bottom (i.e. ^ is v, …).
	 * This can be "fixed" by always doing .opposite in the horizontalFacing calculation, but makes the most sense this way,
	 * especially when it comes to respecting shading of the block textures.
	 *
	 * @param side The side to translate to
	 * @param x The x coordinate of the current rendering
	 * @param y The y coordinate of the current rendering
	 * @param z The z coordinate of the current rendering
	 * @see EnumFacing.glRotate
	 */
	fun translateToSide(side: EnumFacing, x: Double, y: Double, z: Double) {
		if(side.axis === EnumFacing.Axis.Y) {
			// note: with this impl, the texture on side === DOWN is technically flipped top to bottom (i.e. ^ is v, …), this can be "fixed" by always doing .opposite here
			val horizontalFacing = Minecraft.getMinecraft().player.horizontalFacing.let {
				if(side === EnumFacing.UP)
					it.opposite
				else
					it
			}
			GlStateManager.translate(x + horizontalFacing.glOffsetX, y + .5, z + horizontalFacing.glOffsetZ)
			side.glRotate()
			GlStateManager.rotate(horizontalFacing.glRotationAngle - if(side == EnumFacing.DOWN && horizontalFacing.axis == EnumFacing.Axis.Z) 180f else 0f, 0f, 0f, 1f)
		} else {
			GlStateManager.translate(x + .5, y + 1, z + .5)
			side.glRotate()
		}
		GlStateManager.translate(.0, .0, .5)
	}
}

