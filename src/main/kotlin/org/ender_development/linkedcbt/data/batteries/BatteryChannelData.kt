package org.ender_development.linkedcbt.data.batteries

import org.ender_development.linkedcbt.LCBTConfig
import org.ender_development.linkedcbt.data.DimBlockPos
import org.ender_development.linkedcbt.data.base.BaseChannelData
import org.ender_development.linkedcbt.data.batteries.client.ClientBatteryChannelData
import java.util.*

/** **Do NOT** use [ownerUsername] nor [name] for any checking */
data class BatteryChannelData(override var deleted: Boolean, override var ownerUUID: UUID, override var ownerUsername: String, override var name: String, var energyAmount: Int, override val linkedPositions: HashSet<DimBlockPos>) : BaseChannelData<BatteryChannelData, ClientBatteryChannelData>() {
	/**
	 * Used client-side to make stuff display properly
	 */
	var energyCapacityOverride = 0

	val energyCapacity: Int
		get() = if(energyCapacityOverride != 0)
			energyCapacityOverride
		else
			LCBTConfig.batteries.capacity * if(LCBTConfig.batteries.capacityChangesWithBatteryCount) linkedPositions.size else 1

	override fun toClientChannelData(id: Int) =
		ClientBatteryChannelData(id, name)
}
