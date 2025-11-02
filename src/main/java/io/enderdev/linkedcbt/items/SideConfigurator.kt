package io.enderdev.linkedcbt.items

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.Tags
import io.enderdev.linkedcbt.client.gui.BaseLinkedGui
import io.enderdev.linkedcbt.tiles.BaseLinkedTile
import io.enderdev.linkedcbt.tiles.TileLinkedTank
import io.enderdev.linkedcbt.util.extensions.reply
import io.enderdev.linkedcbt.util.extensions.replyFail
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.ender_development.catalyx.items.BaseItem
import org.ender_development.catalyx.utils.extensions.colorValue

class SideConfigurator : BaseItem(LinkedCBT, "side_configurator") {
	init {
		maxStackSize = 1
	}

	override fun onItemUseFirst(player: EntityPlayer, world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, hand: EnumHand): EnumActionResult {
		if(world.isRemote || hand != EnumHand.MAIN_HAND)
			return EnumActionResult.PASS

		val te = world.getTileEntity(pos) as? BaseLinkedTile<*, *, *, *> ?: return EnumActionResult.PASS
		if(te.channelData?.canBeEditedBy(player.uniqueID) == false) {
			player.replyFail(TextComponentTranslation("info.${Tags.MOD_ID}:no_permission"))
			return EnumActionResult.FAIL
		}

		val newSideConfiguration = te.sideConfiguration[facing].let {
			if(player.isSneaking)
				it.previous()
			else
				it.next()
		}
		te.sideConfiguration[facing] = newSideConfiguration
		player.reply(newSideConfiguration.describe(facing, te.facing), newSideConfiguration.colour)

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

		val te = mc.world.getTileEntity(mousedOver.blockPos ?: return) as? BaseLinkedTile<*, *, *, *> ?: return
		val res = ev.resolution
		val facing = mousedOver.sideHit
		val side = te.sideConfiguration[facing]

		GlStateManager.pushMatrix()
		GlStateManager.color(1f, 1f, 1f, 1f)

		val text = side.describe(facing, te.facing)
		val w = BaseLinkedGui.FONT_RENDERER.getStringWidth(text)
		val x = (res.scaledWidth - w) shr 1
		val y = res.scaledHeight - OVERLAY_OFF_Y
		Gui.drawRect(x - 2, y - 2, x + w + 2, y + BaseLinkedGui.FONT_HEIGHT + 2, OVERLAY_BG)
		BaseLinkedGui.FONT_RENDERER.drawString(text, x, y, side.colour.colorValue)

		GlStateManager.popMatrix()
	}

	private companion object {
		const val OVERLAY_OFF_Y = 40 // hotbar is like 22 iirc (see GuiIngame#renderHotbar <L526>)
		const val OVERLAY_BG = 0x7f0c1824 + 0x20000000 // for some reason kotlin hates making this normal (i.e. it hates when it begins with anything more than 7F), so have to do this cursed thing
	}
}
