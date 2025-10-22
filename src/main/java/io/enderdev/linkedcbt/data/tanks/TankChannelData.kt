package io.enderdev.linkedcbt.data.tanks

import io.enderdev.linkedcbt.LCBTConfig
import io.enderdev.linkedcbt.data.DimBlockPos
import io.enderdev.linkedcbt.data.base.BaseChannelData
import io.enderdev.linkedcbt.data.tanks.client.ClientTankChannelData
import net.minecraftforge.fluids.Fluid
import java.util.*

/*
 * We have to keep [deleted] channels in the channelData because otherwise there could be a situation where:
 * - someone places a linked tank somewhere and links it to channel id X
 * - they go somewhere else and that linked tank unloads
 * - then they delete the channel with id X
 * - someone *different* creates a channel and gets channel id X
 * - the first linked tank gets loaded and is magically linked to the new channel
 * This is the most resilient way of ensuring this doesn't happen, sadly it also wastes a little bit of space, but I think that's a sacrifice worth paying
 */
/** **Do NOT** use [ownerUsername] nor [name] for any checking */
data class TankChannelData(override var deleted: Boolean, override var ownerUUID: UUID, override var ownerUsername: String, override var name: String, var fluid: Fluid?, var fluidAmount: Int, override val linkedPositions: HashSet<DimBlockPos>) : BaseChannelData<TankChannelData, ClientTankChannelData>() {
	/**
	 * Used clientside to make stuff display properly
	 */
	var fluidCapacityOverride = 0

	val fluidCapacity: Int
		get() = if(fluidCapacityOverride != 0)
			fluidCapacityOverride
		else
			LCBTConfig.tanks.capacity * if(LCBTConfig.tanks.capacityChangesWithTankCount) linkedPositions.size else 1

	override fun toClientChannelData(id: Int) =
		ClientTankChannelData(id, name, fluid, fluidAmount, fluidCapacity)
}
