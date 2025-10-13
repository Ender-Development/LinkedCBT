package io.enderdev.linkedtanks.tiles.util

import io.enderdev.linkedtanks.tiles.TileLinkedTank
import io.enderdev.linkedtanks.util.FluidUtils
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fluids.capability.IFluidHandler
import org.ender_development.catalyx.tiles.BaseTile.Companion.FLUID_CAP

// this could be generalized and thrown into Catalyx ;p
class FluidSideConfiguration(val tile: TileLinkedTank) {
	val sides = Object2ObjectOpenHashMap<EnumFacing, Side>(EnumFacing.entries.size).apply {
		EnumFacing.entries.forEach {
			put(it, Side.DEFAULT)
		}
	}

	val fillOnlyWrapper = FluidUtils.FillOnlyWrapper(tile.fluidHandler)
	val drainOnlyWrapper = FluidUtils.DrainOnlyWrapper(tile.fluidHandler)

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

			if(tile.fluidHandler.fluidAmount <= 0)
				break

			val te = tile.world.getTileEntity(tile.pos.offset(facing)) ?: continue
			val cap = te.getCapability(FLUID_CAP, facing.opposite) ?: continue
			if(side == Side.PUSH)
				tile.fluidHandler.drain(cap.fill(tile.fluidHandler.fluid, true), true)
			else // Side.PULL
				tile.fluidHandler.fill(cap.drain(tile.fluidHandler.capacity - tile.fluidHandler.fluidAmount, true), true)
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

	fun writeToNBT(): NBTTagCompound {
		val tag = NBTTagCompound()

		sides.forEach { (facing, side) ->
			if(side == Side.NONE)
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
		DEFAULT("default", TextFormatting.LIGHT_PURPLE),
		/**
		 * No inputting or outputting may occur
		 */
		NONE("none", TextFormatting.GRAY),
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

		fun next() =
			when(this) {
				DEFAULT -> NONE
				NONE -> INPUT
				INPUT -> PULL
				PULL -> OUTPUT
				OUTPUT -> PUSH
				PUSH -> DEFAULT
			}
	}
}
