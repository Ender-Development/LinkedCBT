package org.ender_development.linkedcbt.data.batteries.client

import org.ender_development.linkedcbt.data.Constants
import org.ender_development.linkedcbt.data.base.client.ClientBaseChannelData
import org.ender_development.linkedcbt.data.batteries.BatteryChannelData
import net.minecraft.client.Minecraft

data class ClientBatteryChannelData(override val id: Int, override val name: String) : ClientBaseChannelData<ClientBatteryChannelData, BatteryChannelData>() {
	override fun toFakeChannelData() =
		BatteryChannelData(false, Minecraft.getMinecraft().player.uniqueID, Minecraft.getMinecraft().player.gameProfile.name, name, -1, Constants.NO_LINKED_POSITIONS)
}
