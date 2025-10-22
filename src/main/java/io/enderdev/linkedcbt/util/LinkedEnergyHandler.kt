package io.enderdev.linkedcbt.util

import io.enderdev.linkedcbt.data.batteries.BatteryChannelData
import net.minecraftforge.energy.IEnergyStorage

class LinkedEnergyHandler(override var channelData: BatteryChannelData?) : BaseLinkedHandler<IEnergyStorage, BatteryChannelData>(), IEnergyStorage {
	override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
		if(maxReceive <= 0)
			return 0

		val channelData = channelData ?: return 0
		if(channelData.energyAmount >= channelData.energyCapacity)
			return 0

		val received = maxReceive.coerceAtMost(channelData.energyCapacity - channelData.energyAmount)

		if(!simulate)
			channelData.energyAmount += received

		return received
	}

	override fun extractEnergy(maxExtract: Int, simulate: Boolean): Int {
		if(maxExtract <= 0)
			return 0

		val channelData = channelData ?: return 0
		if(channelData.energyAmount <= 0)
			return 0

		val extracted = channelData.energyAmount.coerceIn(0, maxExtract)

		if(!simulate)
			channelData.energyAmount -= extracted

		return extracted
	}

	override fun getEnergyStored() =
		channelData?.energyAmount ?: 0

	override fun getMaxEnergyStored() =
		channelData?.energyCapacity ?: 0

	override fun canExtract() =
		channelData != null

	override fun canReceive() =
		channelData != null
}
