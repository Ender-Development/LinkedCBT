package org.ender_development.linkedcbt.data.tanks

import org.ender_development.linkedcbt.LCBTConfig
import org.ender_development.linkedcbt.data.DimBlockPos
import org.ender_development.linkedcbt.data.base.BaseChannelData
import org.ender_development.linkedcbt.data.tanks.client.ClientTankChannelData
import net.minecraftforge.fluids.Fluid
import java.util.*

data class TankChannelData(
	override var deleted: Boolean,
	override var ownerUUID: UUID,
	override var ownerUsername: String,
	override var name: String,
	var fluid: Fluid?,
	var fluidAmount: Int,
	override val linkedPositions: HashSet<DimBlockPos>,
	override val creationTime: Long
) : BaseChannelData<TankChannelData, ClientTankChannelData>() {
	/**
	 * Used clientside to make stuff display properly
	 */
	var fluidCapacityOverride = 0

	val fluidCapacity: Int
		get() = if(fluidCapacityOverride != 0)
			fluidCapacityOverride
		else
			LCBTConfig.tanks.capacity * if(LCBTConfig.tanks.capacityChangesWithTankCount) linkedPositions.size else 1

	override fun toClientChannelData(id: UUID) =
		ClientTankChannelData(id, name, creationTime)
}
