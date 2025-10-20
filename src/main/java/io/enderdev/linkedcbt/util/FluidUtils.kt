package io.enderdev.linkedcbt.util

import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler

object FluidUtils {
	class FillOnlyWrapper(val wrapped: IFluidHandler) : IFluidHandler by wrapped {
		override fun drain(maxDrain: Int, doDrain: Boolean) =
			null

		override fun drain(resource: FluidStack?, doDrain: Boolean) =
			null
	}

	class DrainOnlyWrapper(val wrapped: IFluidHandler) : IFluidHandler by wrapped {
		override fun fill(resource: FluidStack?, doFill: Boolean) =
			0
	}
}
