package io.enderdev.linkedcbt.data.chests.client

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.base.client.ClientBaseChannelData
import io.enderdev.linkedcbt.data.chests.ChestChannelData
import net.minecraft.client.Minecraft

data class ClientChestChannelData(override val id: Int, override val name: String) : ClientBaseChannelData<ClientChestChannelData, ChestChannelData>() {
	override fun toFakeChannelData() =
		ChestChannelData(false, Minecraft.getMinecraft().player.uniqueID, Minecraft.getMinecraft().player.gameProfile.name, name, emptyArray(), Constants.NO_LINKED_POSITIONS)
}
