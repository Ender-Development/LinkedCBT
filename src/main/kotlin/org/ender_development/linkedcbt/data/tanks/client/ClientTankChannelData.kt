package org.ender_development.linkedcbt.data.tanks.client

import org.ender_development.linkedcbt.data.Constants
import org.ender_development.linkedcbt.data.base.client.ClientBaseChannelData
import org.ender_development.linkedcbt.data.tanks.TankChannelData
import net.minecraft.client.Minecraft

data class ClientTankChannelData(override val id: Int, override val name: String): ClientBaseChannelData<ClientTankChannelData, TankChannelData>() {
	override fun toFakeChannelData() =
		TankChannelData(false, Minecraft.getMinecraft().player.uniqueID, Minecraft.getMinecraft().player.gameProfile.name, name, null, -1, Constants.NO_LINKED_POSITIONS)
}
