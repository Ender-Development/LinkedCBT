package io.enderdev.linkedcbt.client

import io.enderdev.linkedcbt.data.ChannelData
import io.enderdev.linkedcbt.data.Constants
import net.minecraft.client.Minecraft
import net.minecraftforge.fluids.Fluid

data class ClientChannelData(val id: Int, val name: String, val fluid: Fluid?, val fluidAmount: Int, val fluidCapacity: Int) {
	val displayName = if(id == Constants.CREATE_NEW_CHANNEL) "+ $name" else "#$id $name"

	fun toFakeChannelData() =
		ChannelData(false, Minecraft.getMinecraft().player.uniqueID, Minecraft.getMinecraft().player.gameProfile.name, name, fluid, fluidAmount, Constants.NO_LINKED_POSITIONS).apply {
			fluidCapacityOverride = fluidCapacity
		}
}
