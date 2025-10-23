package io.enderdev.linkedcbt.client.gui

import io.enderdev.linkedcbt.client.container.ContainerLinkedChest
import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.chests.ChestChannelData
import io.enderdev.linkedcbt.data.chests.client.ClientChestChannelData
import io.enderdev.linkedcbt.data.chests.client.ClientChestChannelListManager
import io.enderdev.linkedcbt.tiles.TileLinkedChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.inventory.IInventory

class GuiLinkedChest(playerInv: IInventory, tile: TileLinkedChest) : BaseLinkedGui<ChestChannelData, ClientChestChannelData, TileLinkedChest>(ContainerLinkedChest(playerInv, tile), tile, ClientChestChannelListManager) {
	override val displayWrapper = null

	var displayingContents = false

	override fun customDrawMainOverview(partialTicks: Float, mouseX: Int, mouseY: Int) {
		displayingContents = isHovered(guiLeft + BAR_BACKGROUND_X, guiTop + BAR_BACKGROUND_Y, BAR_BACKGROUND_W, BAR_BACKGROUND_H, mouseX, mouseY)
	}

	override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY)
		if(!displayingContents || currentDisplay != CurrentDisplay.MAIN_OVERVIEW)
			return

		mc.textureManager.bindTexture(Constants.LINKED_CBT_GUI)
		GlStateManager.color(1f, 1f, 1f, 1f)

		// I ran out of space in the texture so have to do this sadly
		// draw the inventory
		drawTexturedModalRect(BAR_BACKGROUND_X, BAR_BACKGROUND_Y, INVENTORY_U, INVENTORY_V, INVENTORY_W, INVENTORY_ROW_H * 3)
		// in case I wanna draw a 4th row
		// drawTexturedModalRect(BAR_BACKGROUND_X, BAR_BACKGROUND_Y + INVENTORY_ROW_H * 3, INVENTORY_U, INVENTORY_V, INVENTORY_W, INVENTORY_ROW_H)
		// draw a slight background overlay to hide the buttons behind it
		drawRectWH(BAR_BACKGROUND_X, BAR_BACKGROUND_Y + INVENTORY_ROW_H * 3, INVENTORY_W, INVENTORY_ROW_H * 2, BG_COLOUR)
		val channelData = tile.channelData ?: return
		RenderHelper.enableGUIStandardItemLighting()
		channelData.items.forEachIndexed { idx, stack ->
			if(stack.isEmpty)
				return@forEachIndexed

			val col = idx % 9
			val row = idx / 9
			val x = BAR_BACKGROUND_X + (18 * col)
			val y = BAR_BACKGROUND_Y + 1 + (18 * row)
			itemRender.renderItemIntoGUI(stack, x, y)
			itemRender.renderItemOverlayIntoGUI(FONT_RENDERER, stack, x, y, null)
		}
		RenderHelper.disableStandardItemLighting()
	}

	@Suppress("NOTHING_TO_INLINE", "SameParameterValue")
	private inline fun drawRectWH(x: Int, y: Int, w: Int, h: Int, colour: Int) =
		drawRect(x, y, x + w, y + h, colour)

	companion object {
		const val INVENTORY_U = 8
		const val INVENTORY_V = 96
		const val INVENTORY_W = 162
		const val INVENTORY_ROW_H = 18
		const val BG_COLOUR = 0x7f0c1824
	}
}
