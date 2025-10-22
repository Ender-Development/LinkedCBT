package io.enderdev.linkedcbt.tiles.util

import io.enderdev.linkedcbt.LCBTConfig
import io.enderdev.linkedcbt.tiles.TileLinkedTank
import io.enderdev.linkedcbt.util.FluidUtils
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import org.ender_development.catalyx.tiles.BaseTile.Companion.FLUID_CAP
import org.ender_development.catalyx.utils.Delegates

// this could be generalized and thrown into Catalyx ;p
class FluidSideConfiguration(val tile: TileLinkedTank) : BaseSideConfiguration<IFluidHandler>() {
	override val handler by Delegates.lazyProperty(tile::fluidHandler)
	override val inputOnlyWrapper by Delegates.lazyProperty {
		FluidUtils.FillOnlyWrapper(handler)
	}
	override val outputOnlyWrapper by Delegates.lazyProperty {
		FluidUtils.DrainOnlyWrapper(handler)
	}

	override fun tick() {
		for((facing, side) in sides) {
			if(side != SideConfiguration.PUSH && side != SideConfiguration.PULL)
				continue

			if((side == SideConfiguration.PUSH && handler.fluidAmount <= 0) || (side == SideConfiguration.PULL && handler.fluidAmount >= handler.capacity))
				continue

			val te = tile.world.getTileEntity(tile.pos.offset(facing)) ?: continue
			if(te is TileLinkedTank && te.channelId == tile.channelId) // pointless looping
				continue

			val cap = te.getCapability(FLUID_CAP, facing.opposite) ?: continue
			if(side == SideConfiguration.PUSH)
				handler.drain(cap.fill(handler.fluid?.let {
					if(LCBTConfig.tanks.maxPushPullThroughput != 0)
						FluidStack(it.fluid, it.amount.coerceAtMost(LCBTConfig.tanks.maxPushPullThroughput))
					else
						it
				}, true), true)
			else { // SideConfiguration.PULL
				val maxDrain = (handler.capacity - handler.fluidAmount).let {
					if(LCBTConfig.tanks.maxPushPullThroughput != 0)
						it.coerceIn(0, LCBTConfig.tanks.maxPushPullThroughput)
					else
						it
				}

				val currentFluid = handler.fluid
				val wouldDrain = if(currentFluid == null)
					cap.drain(maxDrain, false)
				else
					cap.drain(FluidStack(currentFluid, maxDrain), false)

				if(wouldDrain == null || wouldDrain.amount <= 0 || (currentFluid != null && wouldDrain.fluid != currentFluid.fluid))
					continue

				val filled = handler.fill(wouldDrain, true)
				if(filled <= 0) // shouldn't happen
					continue

				cap.drain(FluidStack(wouldDrain.fluid, filled), true)
			}
		}
	}
}
