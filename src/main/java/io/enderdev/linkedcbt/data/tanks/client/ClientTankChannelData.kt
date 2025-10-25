package io.enderdev.linkedcbt.data.tanks.client

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.base.client.ClientBaseChannelData
import io.enderdev.linkedcbt.data.tanks.TankChannelData
import net.minecraft.client.Minecraft

data class ClientTankChannelData(override val id: Int, override val name: String): ClientBaseChannelData<ClientTankChannelData, TankChannelData>() {
	override fun toFakeChannelData() =
		TankChannelData(false, Minecraft.getMinecraft().player.uniqueID, Minecraft.getMinecraft().player.gameProfile.name, name, null, -1, Constants.NO_LINKED_POSITIONS)
}
