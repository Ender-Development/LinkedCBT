package org.ender_development.linkedcbt.tiles

import org.ender_development.linkedcbt.blocks.LinkedBatteryBlock
import org.ender_development.linkedcbt.data.Constants
import org.ender_development.linkedcbt.data.batteries.BatteryChannelData
import org.ender_development.linkedcbt.data.batteries.LBPersistentData
import org.ender_development.linkedcbt.tiles.util.EnergySideConfiguration
import org.ender_development.linkedcbt.util.LinkedEnergyHandler
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.energy.IEnergyStorage
import org.ender_development.catalyx.core.tiles.helper.IEnergyTile
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

	val currentlyHasEnergy
		inline get() = channelData?.let { it.energyAmount > 0 } ?: false

	var hadEnergyPreviously = false
	// change the behaviour of super@BaseLinkedTile without overriding everything because this is easier
	override fun markDirtyGUI() {
		val currentlyHasEnergy = currentlyHasEnergy
		if(world != null && currentlyHasEnergy != hadEnergyPreviously) {
			// similar to [markDirtyClient]
			world.setBlockState(pos, world.getBlockState(pos).withProperty(LinkedBatteryBlock.hasEnergy, currentlyHasEnergy), 2)
			markDirty()
			hadEnergyPreviously = currentlyHasEnergy
		} else
			super.markDirtyGUI()
	}
}
