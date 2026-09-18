package org.ender_development.linkedcbt.tiles

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.wrapper.EmptyHandler
import org.ender_development.linkedcbt.data.Constants
import org.ender_development.linkedcbt.data.chests.ChestChannelData
import org.ender_development.linkedcbt.data.chests.LCPersistentData
import org.ender_development.linkedcbt.tiles.util.ItemSideConfiguration
import org.ender_development.linkedcbt.util.LinkedItemHandler
import java.util.*

class TileLinkedChest : BaseLinkedTile<TileLinkedChest, ChestChannelData, IItemHandler, LinkedItemHandler>(LCPersistentData, ITEM_CAP) {
	override val sideConfiguration = ItemSideConfiguration(this)
	override val linkedHandler = LinkedItemHandler(channelData)

	override fun writeClientChannelData(channelData: ChestChannelData, tag: NBTTagCompound) {
		channelData.items.forEachIndexed { idx, stack ->
			tag.setTag("Item$$idx", stack.writeToNBT(NBTTagCompound()))
		}
	}

	override fun readClientChannelData(tag: NBTTagCompound, name: String, ownerUsername: String, ownerUUID: UUID, creationTime: Long) =
        ChestChannelData(
            false,
            ownerUUID,
            ownerUsername,
            name,
            Array(Constants.LINKED_CHEST_INVENTORY_SIZE) { ItemStack(tag.getCompoundTag("Item$$it")) },
            Constants.NO_LINKED_POSITIONS,
			creationTime
        )

	override val inventorySlotCount = Constants.LINKED_CHEST_INVENTORY_SIZE
	override val inventory = linkedHandler
}
