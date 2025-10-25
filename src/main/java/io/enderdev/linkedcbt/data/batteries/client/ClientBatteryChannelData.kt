package io.enderdev.linkedcbt.data.batteries.client

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.base.client.ClientBaseChannelData
import io.enderdev.linkedcbt.data.batteries.BatteryChannelData
import net.minecraft.client.Minecraft

data class ClientBatteryChannelData(override val id: Int, override val name: String) : ClientBaseChannelData<ClientBatteryChannelData, BatteryChannelData>() {
	override fun toFakeChannelData() =
		BatteryChannelData(false, Minecraft.getMinecraft().player.uniqueID, Minecraft.getMinecraft().player.gameProfile.name, name, -1, Constants.NO_LINKED_POSITIONS)
}
