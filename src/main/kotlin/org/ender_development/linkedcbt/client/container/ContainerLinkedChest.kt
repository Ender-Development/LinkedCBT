package org.ender_development.linkedcbt.client.container

import net.minecraft.inventory.IInventory
import org.ender_development.catalyx.core.client.container.BaseContainer
import org.ender_development.linkedcbt.client.gui.BaseLinkedGui.Companion.BAR_BACKGROUND_X
import org.ender_development.linkedcbt.client.gui.GuiLinkedChest.Companion.INVENTORY_H
import org.ender_development.linkedcbt.tiles.TileLinkedChest

class ContainerLinkedChest(playerInv: IInventory, tile: TileLinkedChest) : BaseContainer(playerInv, tile) {
	init {
		// copied addSlotArray(BAR_BACKGROUND_X, 6 - INVENTORY_H, 3, 9, tile.inventory) but I need to instantiate a custom Slot
		var index = 0
		repeat(3) { row ->
			repeat(9) { column ->
				addSlotToContainer(SlotItemHandlerDisableable(tile.inventory, index++, BAR_BACKGROUND_X + 18 * column, 6 - INVENTORY_H + 18 * row))
			}
		}
	}
}
