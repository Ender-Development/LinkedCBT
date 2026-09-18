package org.ender_development.linkedcbt.data.chests.client

import org.ender_development.linkedcbt.data.Constants
import org.ender_development.linkedcbt.data.base.client.ClientBaseChannelData
import org.ender_development.linkedcbt.data.chests.ChestChannelData
import net.minecraft.client.Minecraft
import java.util.UUID

data class ClientChestChannelData(override val id: UUID, override val name: String, override val creationTime: Long) : ClientBaseChannelData<ClientChestChannelData, ChestChannelData>() {
	override fun toFakeChannelData() =
		ChestChannelData(false, Minecraft.getMinecraft().player.uniqueID, Minecraft.getMinecraft().player.gameProfile.name, name, emptyArray(), Constants.NO_LINKED_POSITIONS, creationTime)
}
