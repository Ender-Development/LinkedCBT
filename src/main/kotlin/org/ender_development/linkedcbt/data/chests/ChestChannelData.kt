package org.ender_development.linkedcbt.data.chests

import org.ender_development.linkedcbt.data.DimBlockPos
import org.ender_development.linkedcbt.data.base.BaseChannelData
import org.ender_development.linkedcbt.data.chests.client.ClientChestChannelData
import net.minecraft.item.ItemStack
import java.util.*

data class ChestChannelData(override var deleted: Boolean, override var ownerUUID: UUID, override var ownerUsername: String, override var name: String, val items: Array<ItemStack>, override val linkedPositions: HashSet<DimBlockPos>) : BaseChannelData<ChestChannelData, ClientChestChannelData>() {
	override fun equals(other: Any?) =
		this === other || (other is ChestChannelData && deleted == other.deleted && ownerUUID == other.ownerUUID && name == other.name && items.contentEquals(other.items) && linkedPositions == other.linkedPositions)

	override fun hashCode() =
		Objects.hash(deleted, ownerUUID, name, items.contentHashCode(), linkedPositions)

	override fun toClientChannelData(id: Int) =
		ClientChestChannelData(id, name)
}
