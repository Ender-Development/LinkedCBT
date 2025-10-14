package io.enderdev.linkedtanks.items

import io.enderdev.linkedtanks.LinkedTanks
import io.enderdev.linkedtanks.client.gui.GuiLinkedTank
import io.enderdev.linkedtanks.tiles.TileLinkedTank
import io.enderdev.linkedtanks.util.extensions.reply
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.ender_development.catalyx.items.BaseItem

class ItemTankConfigurator : BaseItem(LinkedTanks.modSettings, "tank_configurator") {
	init {
		maxStackSize = 1
	}

	override fun onItemUseFirst(player: EntityPlayer, world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, hand: EnumHand): EnumActionResult {
		if(world.isRemote || hand != EnumHand.MAIN_HAND)
			return EnumActionResult.PASS

		val te = world.getTileEntity(pos) as? TileLinkedTank ?: return EnumActionResult.PASS
		if(te.channelData?.canBeEditedBy(player.uniqueID) == false)
			return EnumActionResult.PASS

		val newSideConfiguration = te.fluidSideConfiguration[facing].let {
			if(player.isSneaking)
				it.previous()
			else
				it.next()
		}
		te.fluidSideConfiguration[facing] = newSideConfiguration
		player.reply(newSideConfiguration.describe(facing), newSideConfiguration.colour)

		return EnumActionResult.SUCCESS
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	fun drawOverlay(ev: RenderGameOverlayEvent.Post) {
		if(ev.type != RenderGameOverlayEvent.ElementType.HOTBAR)
			return

		val mc = Minecraft.getMinecraft()

		val player = mc.player
		if(player.heldItemMainhand.item !== this)
			return

		val mousedOver = mc.objectMouseOver ?: return
		if(mousedOver.typeOfHit != RayTraceResult.Type.BLOCK)
			return

		val te = mc.world.getTileEntity(mousedOver.blockPos ?: return) as? TileLinkedTank ?: return
		val res = ev.resolution
		val facing = mousedOver.sideHit
		val side = te.fluidSideConfiguration[facing]
		val fontRenderer = mc.fontRenderer

		GlStateManager.pushMatrix()
		GlStateManager.color(1f, 1f, 1f, 1f)

		val text = "§${side.colour.formattingCode}${side.describe(facing)}"
		val w = fontRenderer.getStringWidth(text)
		val x = (res.scaledWidth - w) shr 1
		val y = res.scaledHeight - OVERLAY_OFF_Y
		Gui.drawRect(x - 2, y - 2, x + w + 2, y + GuiLinkedTank.FONT_HEIGHT + 2, OVERLAY_BG)
		fontRenderer.drawString(text, x, y, 0)

		GlStateManager.popMatrix()
	}

	private companion object {
		const val OVERLAY_OFF_Y = 40 // hotbar is like 22 iirc (see GuiIngame#renderHotbar <L526>)
		const val OVERLAY_BG = 0x7f0c1824 + 0x20000000 // for some reason kotlin hates making this normal (i.e. it hates when it begins with anything more than 7F), so have to do this cursed thing
	}
}
