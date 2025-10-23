package io.enderdev.linkedcbt.data.chests

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.DimBlockPos
import io.enderdev.linkedcbt.data.base.BasePersistentData
import io.enderdev.linkedcbt.tiles.TileLinkedChest
import io.enderdev.linkedcbt.util.extensions.dim
import io.enderdev.linkedcbt.util.extensions.dimId
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

object LCPersistentData : BasePersistentData<ChestChannelData, TileLinkedChest>("chests") {
	override fun readChannel(tag: NBTTagCompound): ChestChannelData {
		val deleted = tag.getBoolean("Deleted")
		val ownerUUID = tag.getUniqueId("OwnerUUID")!!
		val ownerUsername = tag.getString("OwnerUsername")
		val name = tag.getString("Name")
		val linkedPositionCount = tag.getInteger("LinkedPositionCount")
		val linkedPositions = HashSet<DimBlockPos>(linkedPositionCount)
		(0..<linkedPositionCount).mapTo(linkedPositions) {
			DimBlockPos.fromString(tag.getString("LinkedPosition$$it"))
		}
		val items = Array(Constants.LINKED_CHEST_INVENTORY_SIZE) { idx ->
			if(!tag.hasKey("Item$$idx"))
				ItemStack.EMPTY
			else
				ItemStack(tag.getCompoundTag("Item$$idx"))
		}

		return ChestChannelData(deleted, ownerUUID, ownerUsername, name, items, linkedPositions)
	}

	override fun writeChannel(channelData: ChestChannelData) =
		NBTTagCompound().apply {
			setBoolean("Deleted", channelData.deleted)
			setUniqueId("OwnerUUID", channelData.ownerUUID)
			setString("OwnerUsername", channelData.ownerUsername)
			setString("Name", channelData.name)
			setInteger("LinkedPositionCount", channelData.linkedPositions.size)
			channelData.linkedPositions.forEachIndexed { idx, pos ->
				setString("LinkedPosition$$idx", pos.toString())
			}
			channelData.items.forEachIndexed { idx, stack ->
				if(!stack.isEmpty)
					setTag("Item$$idx", stack.writeToNBT(NBTTagCompound()))
			}
		}

	override fun createEmptyChannel(player: EntityPlayer, te: TileLinkedChest, channelName: String?) =
		ChestChannelData(false, player.uniqueID, player.gameProfile.name, channelName ?: "New channel $nextChannelId", Array(Constants.LINKED_CHEST_INVENTORY_SIZE) { ItemStack.EMPTY }, hashSetOf(te.pos dim te.world.dimId))
}
