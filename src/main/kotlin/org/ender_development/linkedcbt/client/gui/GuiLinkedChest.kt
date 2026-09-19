package org.ender_development.linkedcbt.client.gui

import org.ender_development.linkedcbt.client.container.ContainerLinkedChest
import org.ender_development.linkedcbt.data.chests.ChestChannelData
import org.ender_development.linkedcbt.data.chests.client.ClientChestChannelData
import org.ender_development.linkedcbt.data.chests.client.ClientChestChannelListManager
import org.ender_development.linkedcbt.tiles.TileLinkedChest
import net.minecraft.inventory.IInventory
import org.ender_development.linkedcbt.client.container.SlotItemHandlerDisableable

class GuiLinkedChest(playerInv: IInventory, tile: TileLinkedChest) : BaseLinkedGui<ChestChannelData, ClientChestChannelData, TileLinkedChest>(ContainerLinkedChest(playerInv, tile), tile, ClientChestChannelListManager) {
	override val displayWrapper = null

	override fun drawCustomDisplayWrapper(partialTicks: Float, mouseX: Int, mouseY: Int) {
		// I could use this space for something, but for now, it's gonna be blank ;p
		drawTexturedModalRect(guiLeft + BAR_BACKGROUND_X, guiTop + BAR_BACKGROUND_Y, 4, 4, BAR_BACKGROUND_W, BAR_BACKGROUND_H)
	}

	override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY)
		inventorySlots.inventorySlots.forEach {
			if(it is SlotItemHandlerDisableable)
				it.enabled = currentDisplay == CurrentDisplay.MAIN_OVERVIEW && tile.channelData != null
		}
		if(currentDisplay != CurrentDisplay.MAIN_OVERVIEW)
			return

		// extend the GUI vertically
		drawTexturedModalRect(guiLeft, guiTop - INVENTORY_H, 0, 0, xSize, INVENTORY_H + 4)
		// draw the inventory bg
		drawTexturedModalRect(guiLeft + BAR_BACKGROUND_X, guiTop + 5 - INVENTORY_H, INVENTORY_U, INVENTORY_V, INVENTORY_W, INVENTORY_H)
	}

	companion object {
		const val INVENTORY_U = 8
		const val INVENTORY_V = 96
		const val INVENTORY_W = 162
		const val INVENTORY_BOX_WH = 18
		const val INVENTORY_H = INVENTORY_BOX_WH * 3
	}
}
