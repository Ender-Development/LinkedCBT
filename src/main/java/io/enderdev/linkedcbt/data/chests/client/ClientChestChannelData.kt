package io.enderdev.linkedcbt.data.chests.client

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.base.client.ClientBaseChannelData
import io.enderdev.linkedcbt.data.chests.ChestChannelData
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import java.util.*

data class ClientChestChannelData(override val id: Int, override val name: String, val items: Array<ItemStack>) : ClientBaseChannelData<ClientChestChannelData, ChestChannelData>() {
	override fun toFakeChannelData() =
		ChestChannelData(false, Minecraft.getMinecraft().player.uniqueID, Minecraft.getMinecraft().player.gameProfile.name, name, items, Constants.NO_LINKED_POSITIONS)

	override fun equals(other: Any?) =
		this === other || (other is ClientChestChannelData && id == other.id && name == other.name && items.contentEquals(other.items))

	override fun hashCode() =
		Objects.hash(id, name, items.contentHashCode())
}
