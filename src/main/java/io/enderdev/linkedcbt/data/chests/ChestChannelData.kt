package io.enderdev.linkedcbt.data.chests

import io.enderdev.linkedcbt.data.DimBlockPos
import io.enderdev.linkedcbt.data.base.BaseChannelData
import io.enderdev.linkedcbt.data.chests.client.ClientChestChannelData
import net.minecraft.item.ItemStack
import java.util.HashSet
import java.util.Objects
import java.util.UUID

data class ChestChannelData(override var deleted: Boolean, override var ownerUUID: UUID, override var ownerUsername: String, override var name: String, val items: Array<ItemStack>, override val linkedPositions: HashSet<DimBlockPos>) : BaseChannelData<ChestChannelData, ClientChestChannelData>() {
	override fun equals(other: Any?) =
		this === other || (other is ChestChannelData && deleted == other.deleted && ownerUUID == other.ownerUUID && name == other.name && items.contentEquals(other.items) && linkedPositions == other.linkedPositions)

	override fun hashCode() =
		Objects.hash(deleted, ownerUUID, name, items.contentHashCode(), linkedPositions)

	override fun toClientChannelData(id: Int) =
		ClientChestChannelData(id, name, items)
}
