package io.enderdev.linkedcbt.tiles.util

import io.enderdev.linkedcbt.LCBTConfig
import io.enderdev.linkedcbt.tiles.TileLinkedBattery
import io.enderdev.linkedcbt.util.EnergyUtils
import net.minecraftforge.energy.IEnergyStorage
import org.ender_development.catalyx.tiles.BaseTile.Companion.ENERGY_CAP
import org.ender_development.catalyx.utils.Delegates

class EnergySideConfiguration(val tile: TileLinkedBattery) : BaseSideConfiguration<IEnergyStorage>() {
	override val handler by Delegates.lazyProperty(tile::energyStorage)
	override val inputOnlyWrapper by Delegates.lazyProperty {
		EnergyUtils.ReceiveOnlyWrapper(tile.energyStorage)
	}
	override val outputOnlyWrapper by Delegates.lazyProperty {
		EnergyUtils.ExtractOnlyWrapper(tile.energyStorage)
	}

	override fun tick() {
		for((facing, side) in sides) {
			if(side != SideConfiguration.PUSH && side != SideConfiguration.PULL)
				continue

			if((side == SideConfiguration.PUSH && tile.energyStorage.energyStored <= 0) || (side == SideConfiguration.PULL && tile.energyStorage.energyStored >= tile.energyStorage.maxEnergyStored))
				continue

			val te = tile.world.getTileEntity(tile.pos.offset(facing)) ?: continue
			if(te is TileLinkedBattery && te.channelId == tile.channelId) // pointless looping
				continue

			val cap = te.getCapability(ENERGY_CAP, facing.opposite) ?: continue
			if(side == SideConfiguration.PUSH)
				tile.energyStorage.extractEnergy(cap.receiveEnergy(tile.energyStorage.energyStored.let {
					if(LCBTConfig.batteries.maxPushPullThroughput != 0)
						it.coerceAtMost(LCBTConfig.batteries.maxPushPullThroughput)
					else
						it
				}, false), false)
			else // SideConfiguration.PULL
				tile.energyStorage.receiveEnergy(cap.extractEnergy((tile.energyStorage.maxEnergyStored - tile.energyStorage.energyStored).let {
					if(LCBTConfig.batteries.maxPushPullThroughput != 0)
						it.coerceIn(0, LCBTConfig.batteries.maxPushPullThroughput)
					else
						it
				}, false), false)
		}
	}
}
