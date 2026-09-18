package org.ender_development.linkedcbt.data.chests

import org.ender_development.linkedcbt.data.DimBlockPos
import org.ender_development.linkedcbt.data.base.BaseChannelData
import org.ender_development.linkedcbt.data.chests.client.ClientChestChannelData
import net.minecraft.item.ItemStack
import java.util.*

data class ChestChannelData(
	override var ownerUUID: UUID,
	override var ownerUsername: String,
	override var name: String,
	val items: Array<ItemStack>,
	override val linkedPositions: HashSet<DimBlockPos>,
	override val creationTime: Long
) : BaseChannelData<ChestChannelData, ClientChestChannelData>() {
	override fun equals(other: Any?) =
		this === other || (other is ChestChannelData && ownerUUID == other.ownerUUID && name == other.name && items.contentEquals(other.items) && linkedPositions == other.linkedPositions && creationTime == other.creationTime)

	override fun hashCode() =
		Objects.hash(ownerUUID, name, items.contentHashCode(), linkedPositions)

	override fun toClientChannelData(id: UUID) =
		ClientChestChannelData(id, name, creationTime)
}
