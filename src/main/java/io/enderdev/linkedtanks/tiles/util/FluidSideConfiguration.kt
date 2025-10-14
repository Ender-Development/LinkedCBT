package io.enderdev.linkedtanks.tiles.util

import io.enderdev.linkedtanks.LTConfig
import io.enderdev.linkedtanks.tiles.TileLinkedTank
import io.enderdev.linkedtanks.util.FluidUtils
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import org.ender_development.catalyx.tiles.BaseTile.Companion.FLUID_CAP
import org.ender_development.catalyx.utils.Delegates

// this could be generalized and thrown into Catalyx ;p
class FluidSideConfiguration(val tile: TileLinkedTank) {
	val sides = Object2ObjectOpenHashMap<EnumFacing, Side>(EnumFacing.entries.size).apply {
		EnumFacing.entries.forEach {
			put(it, Side.DEFAULT)
		}
	}

	val fillOnlyWrapper by Delegates.lazyProperty {
		FluidUtils.FillOnlyWrapper(tile.fluidHandler)
	}
	val drainOnlyWrapper by Delegates.lazyProperty {
		FluidUtils.DrainOnlyWrapper(tile.fluidHandler)
	}

	@Suppress("NOTHING_TO_INLINE")
	inline operator fun get(side: EnumFacing) =
		sides[side]!!

	@Suppress("NOTHING_TO_INLINE")
	inline operator fun set(side: EnumFacing, value: Side) {
		sides[side] = value
	}

	fun tick() {
		for((facing, side) in sides) {
			if(side != Side.PUSH && side != Side.PULL)
				continue

			if((side == Side.PUSH && tile.fluidHandler.fluidAmount <= 0) || (side == Side.PULL && tile.fluidHandler.fluidAmount >= tile.fluidHandler.capacity))
				continue

			val te = tile.world.getTileEntity(tile.pos.offset(facing)) ?: continue
			if(te is TileLinkedTank && te.channelId == tile.channelId) // pointless looping
				continue

			val cap = te.getCapability(FLUID_CAP, facing.opposite) ?: continue
			if(side == Side.PUSH)
				tile.fluidHandler.drain(cap.fill(tile.fluidHandler.fluid?.let {
					if(LTConfig.maxPushPullThroughput != 0)
						FluidStack(it.fluid, it.amount.coerceAtMost(LTConfig.maxPushPullThroughput))
					else
						it
				}, true), true)
			else { // Side.PULL
				val maxDrain = (tile.fluidHandler.capacity - tile.fluidHandler.fluidAmount).let {
					if(LTConfig.maxPushPullThroughput != 0)
						it.coerceIn(0, LTConfig.maxPushPullThroughput)
					else
						it
				}

				val currentFluid = tile.fluidHandler.fluid
				val wouldDrain = if(currentFluid == null)
					cap.drain(maxDrain, false)
				else
					cap.drain(FluidStack(currentFluid, maxDrain), false)

				if(wouldDrain == null || wouldDrain.amount <= 0 || (currentFluid != null && wouldDrain.fluid != currentFluid.fluid))
					continue

				val filled = tile.fluidHandler.fill(wouldDrain, true)
				if(filled <= 0) // shouldn't happen
					continue

				cap.drain(FluidStack(wouldDrain.fluid, filled), true)
			}
		}
	}

	fun hasCapability(facing: EnumFacing?) =
		facing == null || this[facing] != Side.NONE

	fun getCapability(facing: EnumFacing?): IFluidHandler? {
		if(facing == null)
			return tile.fluidHandler

		return when(this[facing]) {
			Side.DEFAULT -> tile.fluidHandler
			Side.NONE -> null
			Side.INPUT, Side.PULL -> fillOnlyWrapper
			Side.OUTPUT, Side.PUSH -> drainOnlyWrapper
		}
	}

	fun writeToNBT(writeDefault: Boolean): NBTTagCompound {
		val tag = NBTTagCompound()

		sides.forEach { (facing, side) ->
			if(side == Side.DEFAULT && !writeDefault)
				return@forEach

			tag.setString(facing.name, side.name)
		}

		return tag
	}

	fun readFromNBT(tag: NBTTagCompound) {
		sides.forEach { (facing) ->
			if(tag.hasKey(facing.name))
				sides.put(facing, Side.valueOf(tag.getString(facing.name)))
		}
	}

	enum class Side(val named: String, val colour: TextFormatting) {
		/**
		 * The default state of a side, i.e. can input and output
		 */
		DEFAULT("default", TextFormatting.WHITE),
		/**
		 * No inputting or outputting may occur
		 */
		NONE("none", TextFormatting.DARK_GRAY),
		/**
		 * Can only input
		 */
		INPUT("input", TextFormatting.BLUE),
		/**
		 * Will try to pull from the adjacened tile
		 */
		PULL("pull", TextFormatting.AQUA),
		/**
		 * Can only output
		 */
		OUTPUT("output", TextFormatting.YELLOW),
		/**
		 * Will try to push to the adjacened tile
		 */
		PUSH("push", TextFormatting.GOLD);

		fun previous() =
			when(this) {
				DEFAULT -> PUSH
				NONE -> DEFAULT
				INPUT -> NONE
				PULL -> INPUT
				OUTPUT -> PULL
				PUSH -> OUTPUT
			}

		fun next() =
			when(this) {
				DEFAULT -> NONE
				NONE -> INPUT
				INPUT -> PULL
				PULL -> OUTPUT
				OUTPUT -> PUSH
				PUSH -> DEFAULT
			}

		val u: Int
			get() = when(this) {
				DEFAULT, PULL -> 193
				NONE, OUTPUT -> 203
				INPUT, PUSH -> 213
			}

		val v: Int
			get() = when(this) {
				DEFAULT, NONE, INPUT -> 52
				PULL, OUTPUT, PUSH -> 62
			}

		// should this be translatable? maybe.
		fun describe(facing: EnumFacing) =
			"${facing.name.lowercase().replaceFirstChar(Char::uppercaseChar)} - $named"
	}
}
