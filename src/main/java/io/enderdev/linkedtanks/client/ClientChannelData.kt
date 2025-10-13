package io.enderdev.linkedtanks.client

import io.enderdev.linkedtanks.data.ChannelData
import io.enderdev.linkedtanks.data.Constants
import net.minecraft.client.Minecraft
import net.minecraftforge.fluids.Fluid

data class ClientChannelData(val id: Int, val name: String, val fluid: Fluid?, val fluidAmount: Int, val fluidCapacity: Int) {
	fun toFakeChannelData() =
		ChannelData(false, Minecraft.getMinecraft().player.uniqueID, Minecraft.getMinecraft().player.gameProfile.name, name, fluid, fluidAmount, Constants.NO_LINKED_POSITIONS).apply {
			fluidCapacityOverride = fluidCapacity
		}
}
