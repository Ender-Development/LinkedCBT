package io.enderdev.linkedcbt.tiles

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.batteries.BatteryChannelData
import io.enderdev.linkedcbt.data.batteries.LBPersistentData
import io.enderdev.linkedcbt.tiles.util.EnergySideConfiguration
import io.enderdev.linkedcbt.util.LinkedEnergyHandler
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.energy.IEnergyStorage
import org.ender_development.catalyx.tiles.helper.IEnergyTile
import java.util.*

class TileLinkedBattery : BaseLinkedTile<TileLinkedBattery, BatteryChannelData, IEnergyStorage, LinkedEnergyHandler>(LBPersistentData, ENERGY_CAP), IEnergyTile {
	override val sideConfiguration = EnergySideConfiguration(this)
	override val linkedHandler = LinkedEnergyHandler(channelData)
	override val energyStorage = linkedHandler
	override val energyCapacity = linkedHandler.maxEnergyStored

	override fun writeClientChannelData(channelData: BatteryChannelData, tag: NBTTagCompound) {
		tag.setInteger("EnergyAmount", channelData.energyAmount)
		tag.setInteger("EnergyCapacity", channelData.energyCapacity)
	}

	override fun readClientChannelData(tag: NBTTagCompound, name: String, ownerUsername: String, ownerUUID: UUID) =
		BatteryChannelData(false, ownerUUID, ownerUsername, name, tag.getInteger("EnergyAmount"), Constants.NO_LINKED_POSITIONS).apply {
			energyCapacityOverride = tag.getInteger("EnergyCapacity")
		}
}
