package io.enderdev.linkedtanks.data

import io.enderdev.linkedtanks.LTConfig
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
data class ChannelData(var deleted: Boolean, var ownerUUID: UUID, var ownerUsername: String, var name: String, var fluid: Fluid?, var fluidAmount: Int, val linkedPositions: HashSet<DimBlockPos>) {
	/**
	 * Used clientside to make stuff display properly
	 */
	var fluidCapacityOverride = 0

	fun displayName(channelId: Int) =
		"#$channelId $name"

	val fluidCapacity: Int
		get() = if(fluidCapacityOverride != 0)
			fluidCapacityOverride
		else
			LTConfig.tankCapacity * if(LTConfig.tankCapacityChangesWithTankCount) linkedPositions.size else 1

	fun canBeEditedBy(uuid: UUID) =
		ownerUUID == uuid
}
