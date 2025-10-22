package io.enderdev.linkedcbt.data.tanks.client

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.base.client.ClientBaseChannelData
import io.enderdev.linkedcbt.data.tanks.TankChannelData
import net.minecraft.client.Minecraft
import net.minecraftforge.fluids.Fluid

data class ClientTankChannelData(override val id: Int, override val name: String, val fluid: Fluid?, val fluidAmount: Int, val fluidCapacity: Int): ClientBaseChannelData<ClientTankChannelData, TankChannelData>() {
	override fun toFakeChannelData() =
		TankChannelData(false, Minecraft.getMinecraft().player.uniqueID, Minecraft.getMinecraft().player.gameProfile.name, name, fluid, fluidAmount, Constants.NO_LINKED_POSITIONS).apply {
			fluidCapacityOverride = this@ClientTankChannelData.fluidCapacity
		}
}
