package io.enderdev.linkedcbt.tiles

import io.enderdev.linkedcbt.blocks.LinkedBatteryBlock
import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.batteries.BatteryChannelData
import io.enderdev.linkedcbt.data.batteries.LBPersistentData
import io.enderdev.linkedcbt.tiles.util.EnergySideConfiguration
import io.enderdev.linkedcbt.util.LinkedEnergyHandler
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.energy.IEnergyStorage
import org.ender_development.catalyx.tiles.helper.IEnergyTile
import org.ender_development.catalyx.tiles.helper.ITESRTile
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
			world.setBlockState(pos, world.getBlockState(pos).withProperty(LinkedBatteryBlock.hasEnergy, currentlyHasEnergy), 6)
			markDirty()
			hadEnergyPreviously = currentlyHasEnergy
		} else
			super.markDirtyGUI()
	}
}
