package io.enderdev.linkedcbt.util

import net.minecraftforge.energy.IEnergyStorage

object EnergyUtils {
	class ReceiveOnlyWrapper(val wrapped: IEnergyStorage) : IEnergyStorage by wrapped {
		override fun canExtract() =
			false

		override fun extractEnergy(maxReceive: Int, simulate: Boolean) =
			0
	}

	class ExtractOnlyWrapper(val wrapped: IEnergyStorage) : IEnergyStorage by wrapped {
		override fun canReceive() =
			false

		override fun receiveEnergy(maxReceive: Int, simulate: Boolean) =
			0
	}
}
