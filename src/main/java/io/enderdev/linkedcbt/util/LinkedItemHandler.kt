package io.enderdev.linkedcbt.util

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.chests.ChestChannelData
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.ItemHandlerHelper

class LinkedItemHandler(override var channelData: ChestChannelData?) : BaseLinkedHandler<IItemHandler, ChestChannelData>(), IItemHandler {
	override fun getSlots() =
		Constants.LINKED_CHEST_INVENTORY_SIZE

	override fun getStackInSlot(slot: Int): ItemStack =
		channelData?.items?.get(slot) ?: ItemStack.EMPTY

	override fun getSlotLimit(slot: Int) =
		channelData?.items?.get(slot)?.maxStackSize ?: 0

	override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
		val channelData = channelData ?: return stack
		val current = channelData.items[slot]

		if(current.isEmpty) {
			if(!simulate)
				channelData.items[slot] = stack.copy()

			return ItemStack.EMPTY
		}

		if(current.count == current.maxStackSize || !ItemHandlerHelper.canItemStacksStack(stack, current))
			return stack

		if(current.count + stack.count < current.maxStackSize) {
			if(!simulate)
				current.grow(stack.count)
			return ItemStack.EMPTY
		}

		val inserted = stack.count.coerceAtMost(current.maxStackSize - current.count)

		if(!simulate)
			current.grow(inserted)

		return stack.copy().apply {
			shrink(inserted)
		}
	}

	override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
		val channelData = channelData ?: return ItemStack.EMPTY
		val current = channelData.items[slot]

		if(current.isEmpty)
			return ItemStack.EMPTY

		if(amount >= current.count) {
			if(!simulate) {
				channelData.items[slot] = ItemStack.EMPTY
				// if we're not simulating, there's no need to copy this ItemStack
				return current
			}
			return current.copy()
		}

		if(!simulate) {
			current.shrink(amount)
			return current.copy()
		}

		return current.copy().apply {
			shrink(amount)
		}
	}
}
