package io.enderdev.linkedcbt.tiles.util

import io.enderdev.linkedcbt.LCBTConfig
import io.enderdev.linkedcbt.tiles.TileLinkedChest
import io.enderdev.linkedcbt.util.ItemUtils
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler
import org.ender_development.catalyx.tiles.BaseTile.Companion.ITEM_CAP
import org.ender_development.catalyx.utils.Delegates
import org.ender_development.catalyx.utils.extensions.tryInsert

class ItemSideConfiguration(val tile: TileLinkedChest) : BaseSideConfiguration<IItemHandler>() {
	override val handler by Delegates.lazyProperty(tile::linkedHandler)
	override val inputOnlyWrapper by Delegates.lazyProperty {
		ItemUtils.InsertOnlyWrapper(handler)
	}
	override val outputOnlyWrapper by Delegates.lazyProperty {
		ItemUtils.ExtractOnlyWrapper(handler)
	}

	var tickCounter = 0

	override fun tick() {
		if(++tickCounter != LCBTConfig.chests.pushPullEvery)
			return

		tickCounter = 0

		val channelData = handler.channelData
		if(channelData == null)
			return

		val underlyingItems = channelData.items

		for((facing, side) in sides) {
			if(side != SideConfiguration.PUSH && side != SideConfiguration.PULL)
				continue

			if(side == SideConfiguration.PUSH && underlyingItems.all(ItemStack::isEmpty))
				continue

			val te = tile.world.getTileEntity(tile.pos.offset(facing)) ?: continue
			if(te is TileLinkedChest && te.channelId == tile.channelId) // pointless looping
				continue

			val cap = te.getCapability(ITEM_CAP, facing.opposite) ?: continue
			if(side == SideConfiguration.PUSH)
				for(idx in underlyingItems.indices) {
					val stack = underlyingItems[idx]
					if(stack.isEmpty)
						continue

					// tryInsert modifies the passed in `stack`, though it may return ItemStack.EMPTY, so alter the array anyways
					underlyingItems[idx] = cap.tryInsert(stack)
				}
			else // SideConfiguration.PULL
				for(idx in 0..<cap.slots) {
					val stack = cap.getStackInSlot(idx)
					if(stack.isEmpty)
						continue

					val result = handler.tryInsert(stack.copy())
					val extracted = stack.count - result.count
					if(extracted != 0)
						cap.extractItem(idx, extracted, false)
				}
		}
	}
}
