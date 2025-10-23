package io.enderdev.linkedcbt.tiles

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.chests.ChestChannelData
import io.enderdev.linkedcbt.data.chests.LCPersistentData
import io.enderdev.linkedcbt.tiles.util.ItemSideConfiguration
import io.enderdev.linkedcbt.util.LinkedItemHandler
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.wrapper.EmptyHandler
import java.util.*

class TileLinkedChest : BaseLinkedTile<TileLinkedChest, ChestChannelData, IItemHandler, LinkedItemHandler>(LCPersistentData, ITEM_CAP) {
	override val sideConfiguration = ItemSideConfiguration(this)
	override val linkedHandler = LinkedItemHandler(channelData)

	override fun writeClientChannelData(channelData: ChestChannelData, tag: NBTTagCompound) {
		channelData.items.forEachIndexed { idx, stack ->
			tag.setTag("Item$$idx", stack.writeToNBT(NBTTagCompound()))
		}
	}

	override fun readClientChannelData(tag: NBTTagCompound, name: String, ownerUsername: String, ownerUUID: UUID) =
		ChestChannelData(false, ownerUUID, ownerUsername, name, Array(Constants.LINKED_CHEST_INVENTORY_SIZE) { ItemStack(tag.getCompoundTag("Item$$it")) }, Constants.NO_LINKED_POSITIONS)

	// disable Catalyx auto-shift insertion, among other things
	override val SIZE = 0
	override val inventory = EmptyHandler()
}
