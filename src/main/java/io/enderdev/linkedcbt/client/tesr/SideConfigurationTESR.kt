package io.enderdev.linkedcbt.client.tesr

import io.enderdev.linkedcbt.Tags
import io.enderdev.linkedcbt.tiles.BaseLinkedTile
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.ender_development.catalyx.client.tesr.AbstractTESRenderer
import org.ender_development.catalyx.tiles.BaseTile
import org.ender_development.catalyx.utils.RenderUtils
import org.ender_development.catalyx.utils.extensions.glRotate

internal object SideConfigurationTESR : AbstractTESRenderer() {
	override fun render(tileEntity: BaseTile, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) {
		if(tileEntity !is BaseLinkedTile<*, *, *, *>)
			error("how")

		tileEntity.sideConfiguration.sides.forEach { (side, state) ->
			val texture = ResourceLocation(Tags.MOD_ID, "textures/blocks/io/${state.name.lowercase()}.png")
			// TODO: figure out up/down facing rendering
			GlStateManager.pushMatrix()
			GlStateManager.translate(x + .5, y + 1, z + .5)
			side.glRotate()
			GlStateManager.translate(.0, .0, .5)

			super.setLightmapDisabled(true)
			renderTexture(texture)
			super.setLightmapDisabled(false)
			GlStateManager.popMatrix()
		}
	}

	@Suppress("NOTHING_TO_INLINE")
	private inline fun renderTexture(texture: ResourceLocation) {
		GlStateManager.pushMatrix()

		GlStateManager.translate(-.5, .0, .01)
		GlStateManager.scale(TESR_MAGIC_NUMBER, -TESR_MAGIC_NUMBER, TESR_MAGIC_NUMBER)
		GlStateManager.glNormal3f(0f, 0f, 1f)
		GlStateManager.color(1f, 1f, 1f, 1f)

		RenderUtils.bindTexture(texture)
		drawScaledCustomSizeModalRectLegacy(.0, .0, .0, .0, 16.0, 16.0, ONE_BLOCK_WIDTH, ONE_BLOCK_WIDTH, 16.0, 16.0, -1.2)

		GlStateManager.popMatrix()
	}
}

