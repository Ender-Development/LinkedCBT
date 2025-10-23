package io.enderdev.linkedcbt.util

import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler

object ItemUtils {
	class InsertOnlyWrapper(val wrapped: IItemHandler) : IItemHandler by wrapped {
		// Java default methods are not overridden by delegation
		override fun isItemValid(slot: Int, stack: ItemStack) =
			wrapped.isItemValid(slot, stack)

		override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack =
			ItemStack.EMPTY
	}

	class ExtractOnlyWrapper(val wrapped: IItemHandler) : IItemHandler by wrapped {
		// Java default methods are not overridden by delegation
		override fun isItemValid(slot: Int, stack: ItemStack) =
			wrapped.isItemValid(slot, stack)

		override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean) =
			stack
	}
}
