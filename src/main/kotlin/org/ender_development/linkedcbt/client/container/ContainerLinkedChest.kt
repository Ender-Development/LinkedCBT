package org.ender_development.linkedcbt.client.container

import org.ender_development.linkedcbt.tiles.TileLinkedChest
import net.minecraft.inventory.IInventory
import org.ender_development.catalyx.core.client.container.BaseContainer
import org.ender_development.linkedcbt.client.gui.BaseLinkedGui.Companion.BAR_BACKGROUND_X
import org.ender_development.linkedcbt.client.gui.GuiLinkedChest.Companion.INVENTORY_H

class ContainerLinkedChest(playerInv: IInventory, tile: TileLinkedChest) : BaseContainer(playerInv, tile) {
	init {
		addSlotArray(BAR_BACKGROUND_X, 6 - INVENTORY_H, 3, 9, tile.inventory)
	}
}
