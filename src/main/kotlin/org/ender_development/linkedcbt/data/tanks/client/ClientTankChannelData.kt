package org.ender_development.linkedcbt.data.tanks.client

import org.ender_development.linkedcbt.data.Constants
import org.ender_development.linkedcbt.data.base.client.ClientBaseChannelData
import org.ender_development.linkedcbt.data.tanks.TankChannelData
import net.minecraft.client.Minecraft
import java.util.UUID

data class ClientTankChannelData(override val id: UUID, override val name: String, override val creationTime: Long): ClientBaseChannelData<ClientTankChannelData, TankChannelData>() {
	override fun toFakeChannelData() =
		TankChannelData(Minecraft.getMinecraft().player.uniqueID, Minecraft.getMinecraft().player.gameProfile.name, name, null, -1, Constants.NO_LINKED_POSITIONS, creationTime)
}
