package io.enderdev.linkedcbt.client.tesr

import io.enderdev.linkedcbt.LCBTConfig
import io.enderdev.linkedcbt.tiles.TileLinkedTank
import io.enderdev.linkedcbt.util.ConfigHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import org.ender_development.catalyx.client.tesr.AbstractTESRenderer
import org.ender_development.catalyx.config.ConfigHandler
import org.ender_development.catalyx.tiles.BaseTile
import org.ender_development.catalyx.utils.RenderUtils
import org.ender_development.catalyx.utils.extensions.withAlpha
import org.lwjgl.opengl.GL11
import java.awt.Color

internal object LinkedTankTESR : AbstractTESRenderer() {
	val color = ConfigHelper({ LCBTConfig.client.tankOverlayAlpha }) { Color.WHITE.withAlpha(it / 100f) }

	override fun render(te: BaseTile, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) {
		if(te !is TileLinkedTank)
			return

		val fluid = te.fluidHandler.contents
		if(fluid == null)
			return

		val icon = RenderUtils.getStillTexture(fluid) ?: return
		val minU = icon.minU.toDouble() * icon.iconWidth
		val maxU = icon.maxU.toDouble() * icon.iconWidth
		val minV = icon.minV.toDouble() * icon.iconHeight
		val maxV = icon.maxV.toDouble() * icon.iconHeight

		SideConfigurationTESR.translateToSide(te.facing, x, y, z)
		// the beginning is basically SCTESR#renderTexture
		GlStateManager.pushMatrix()

		GlStateManager.shadeModel(if(Minecraft.isAmbientOcclusionEnabled()) GL11.GL_SMOOTH else GL11.GL_FLAT)
		GlStateManager.translate(-.5, .0, .0001)
		GlStateManager.scale(TESR_MAGIC_NUMBER, -TESR_MAGIC_NUMBER, TESR_MAGIC_NUMBER)
		GlStateManager.color(1f, 1f, 1f, 1f)
		GlStateManager.enableBlend()
		RenderUtils.bindBlockTexture()
		drawScaledCustomSizeModalRect(OFFSET_X, OFFSET_Y, minU, minV, (maxU - minU) * WIDTH_SCALE, (maxV - minV) * HEIGHT_SCALE, WIDTH, HEIGHT, 16.0, 16.0, color = color.value)

		GlStateManager.disableBlend()
		GlStateManager.popMatrix()
	}

	const val PX_PER_BLOCK = 16
	const val PX_PER_BLOCK_D = 16.0
	const val PX_OFFSET_X = 6
	const val PX_OFFSET_Y = 3
	const val PX_WIDTH = 4
	const val PX_HEIGHT = 10
	const val ONE_PX = ONE_BLOCK_WIDTH / PX_PER_BLOCK
	const val OFFSET_X = ONE_PX * PX_OFFSET_X
	const val OFFSET_Y = ONE_PX * PX_OFFSET_Y
	const val WIDTH = ONE_PX * PX_WIDTH
	const val HEIGHT = ONE_PX * PX_HEIGHT
	const val WIDTH_SCALE = PX_WIDTH / PX_PER_BLOCK_D
	const val HEIGHT_SCALE = PX_HEIGHT / PX_PER_BLOCK_D
}

