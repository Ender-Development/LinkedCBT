package io.enderdev.linkedcbt.tiles.util

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

abstract class BaseSideConfiguration<HANDLER> {
	val sides = Object2ObjectOpenHashMap<EnumFacing, SideConfiguration>(EnumFacing.entries.size).apply {
		EnumFacing.entries.forEach {
			put(it, SideConfiguration.DEFAULT)
		}
	}

	@Suppress("NOTHING_TO_INLINE")
	inline operator fun get(side: EnumFacing) =
		sides[side]!!

	@Suppress("NOTHING_TO_INLINE")
	inline operator fun set(side: EnumFacing, value: SideConfiguration) {
		sides[side] = value
	}

	abstract val handler: HANDLER
	abstract val inputOnlyWrapper: HANDLER
	abstract val outputOnlyWrapper: HANDLER

	abstract fun tick()

	fun hasCapability(facing: EnumFacing?) =
		facing == null || this[facing] != SideConfiguration.NONE

	fun getCapability(facing: EnumFacing?): HANDLER? {
		if(facing == null)
			return handler

		return when(this[facing]) {
			SideConfiguration.DEFAULT -> handler
			SideConfiguration.NONE -> null
			SideConfiguration.INPUT, SideConfiguration.PULL -> inputOnlyWrapper
			SideConfiguration.OUTPUT, SideConfiguration.PUSH -> outputOnlyWrapper
		}
	}

	fun writeToNBT(writeDefault: Boolean): NBTTagCompound {
		val tag = NBTTagCompound()

		sides.forEach { (facing, side) ->
			if(side == SideConfiguration.DEFAULT && !writeDefault)
				return@forEach

			tag.setString(facing.name, side.name)
		}

		return tag
	}

	fun readFromNBT(tag: NBTTagCompound) {
		sides.forEach { (facing) ->
			if(tag.hasKey(facing.name))
				sides.put(facing, SideConfiguration.valueOf(tag.getString(facing.name)))
		}
	}
}
