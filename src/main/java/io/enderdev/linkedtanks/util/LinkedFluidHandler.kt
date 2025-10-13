package io.enderdev.linkedtanks.util

import io.enderdev.linkedtanks.data.ChannelData
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidTankInfo
import net.minecraftforge.fluids.IFluidTank
import net.minecraftforge.fluids.capability.FluidTankProperties
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidTankProperties

class LinkedFluidHandler(var channelData: ChannelData?) : IFluidHandler, IFluidTank {
	val contents
		get() = channelData?.fluid?.let { FluidStack(it, channelData!!.fluidAmount) }

	val capacity
		@JvmName("getChannelCapacity")
		get() = channelData?.fluidCapacity ?: 0

	// IFluidHandler
	override fun getTankProperties(): Array<out IFluidTankProperties?>? =
		arrayOf(FluidTankProperties(contents, capacity))

	override fun drain(resource: FluidStack?, doDrain: Boolean): FluidStack? {
		if(channelData == null || resource == null || resource.fluid != channelData!!.fluid)
			return null

		return drain(resource.amount, doDrain)
	}

	override fun fill(resource: FluidStack?, doFill: Boolean): Int {
		if(resource == null)
			return 0

		channelData?.let { channelData ->
			if(channelData.fluid != null && channelData.fluid != resource.fluid)
				return 0

			val filled = resource.amount.coerceAtMost(capacity - channelData.fluidAmount)

			if(DEBUG_LOG)
				println("fill(resource=${resource.fluid?.unlocalizedName} ${resource.amount}, doFill=$doFill) => $filled")

			if(doFill) {
				if(DEBUG_LOG)
					println("filling! previous content: ${channelData.fluid?.unlocalizedName} ${channelData.fluidAmount}")

				channelData.fluid = resource.fluid
				channelData.fluidAmount += filled

				if(DEBUG_LOG)
					println("new content: ${channelData.fluid?.unlocalizedName} ${channelData.fluidAmount}")
			}

			return filled
		}
		return 0
	}

	override fun drain(maxDrain: Int, doDrain: Boolean): FluidStack? {
		channelData?.let { channelData ->
			if(channelData.fluid == null || channelData.fluidAmount == 0 || maxDrain <= 0)
				return null

			val drained = channelData.fluidAmount.coerceIn(0, maxDrain)

			if(DEBUG_LOG)
				println("drain(maxDrain=$maxDrain, doDrain=$doDrain) => $drained")

			val currentFluid = channelData.fluid // needed, otherwise if we drain to 0 and `channelData.fluid` gets set to null, FluidStack() will crash with "Cannot create a fluidstack from a null fluid"
			if(doDrain) {
				if(DEBUG_LOG)
					println("draining! previous content: ${channelData.fluid?.unlocalizedName} ${channelData.fluidAmount}")

				channelData.fluidAmount -= drained
				if(channelData.fluidAmount == 0)
					channelData.fluid = null

				if(DEBUG_LOG)
					println("new content: ${channelData.fluid?.unlocalizedName} ${channelData.fluidAmount}")
			}

			return FluidStack(currentFluid, drained)
		}
		return null
	}

	// IFluidTank
	override fun getFluid() =
		contents

	override fun getFluidAmount() =
		channelData?.fluidAmount ?: 0

	override fun getCapacity() =
		capacity

	override fun getInfo() =
		FluidTankInfo(this)

	private companion object {
		const val DEBUG_LOG = false
	}
}
