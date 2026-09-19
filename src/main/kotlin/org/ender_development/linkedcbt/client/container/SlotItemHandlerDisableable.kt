package org.ender_development.linkedcbt.client.container

import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.SlotItemHandler

class SlotItemHandlerDisableable(itemHandler: IItemHandler, index: Int, x: Int, y: Int) : SlotItemHandler(itemHandler, index, x, y) {
	var enabled = true

	override fun isEnabled() =
		enabled
}
