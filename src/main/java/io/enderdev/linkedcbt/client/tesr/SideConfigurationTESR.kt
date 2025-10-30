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

internal object SideConfigurationTESR : AbstractTESRenderer() {
	override fun render(tileEntity: BaseTile, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) {
		if(tileEntity !is BaseLinkedTile<*, *, *, *>)
			error("how")

		tileEntity.sideConfiguration.sides.forEach { (side, state) ->
			val texture = ResourceLocation(Tags.MOD_ID, "textures/blocks/io/${state.name.lowercase()}.png")
			GlStateManager.pushMatrix()
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

			super.setLightmapDisabled(true)
			renderTexture(texture)
			super.setLightmapDisabled(false)
			GlStateManager.popMatrix()
		}
	}

	private val EnumFacing.glRotationAngle: Float
		get() = when(this) {
			EnumFacing.NORTH -> 180f
			EnumFacing.EAST -> 90f
			EnumFacing.SOUTH -> 0f
			EnumFacing.WEST -> -90f
			EnumFacing.UP, EnumFacing.DOWN -> 0f
		}

	private val EnumFacing.glOffsetX
		get() = when(this) {
			EnumFacing.NORTH, EnumFacing.SOUTH -> .5
			EnumFacing.WEST -> 1.0
			else -> .0
		}

	private val EnumFacing.glOffsetZ
		get() = when(this) {
			EnumFacing.NORTH -> 1.0
			EnumFacing.EAST, EnumFacing.WEST -> .5
			else -> .0
		}

	private fun EnumFacing.glRotate() =
		when(this) {
			EnumFacing.NORTH -> GlStateManager.rotate(180f, 0f, 1f, 0f)
			EnumFacing.EAST -> GlStateManager.rotate(90f, 0f, 1f, 0f)
			EnumFacing.SOUTH -> {}
			EnumFacing.WEST -> GlStateManager.rotate(-90f, 0f, 1f, 0f)
			EnumFacing.UP -> GlStateManager.rotate(-90f, 1f, 0f, 0f)
			EnumFacing.DOWN -> GlStateManager.rotate(90f, 1f, 0f, 0f)
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

