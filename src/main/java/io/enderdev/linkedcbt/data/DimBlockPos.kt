package io.enderdev.linkedcbt.data

import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer
import net.minecraftforge.common.DimensionManager

data class DimBlockPos(val dimId: Int, val pos: BlockPos) {
	constructor(dimId: Int, x: Int, y: Int, z: Int) : this(dimId, BlockPos(x, y, z))

	val x: Int
		inline get() = pos.x

	val y: Int
		inline get() = pos.y

	val z: Int
		inline get() = pos.z

	val world: WorldServer?
		inline get() = DimensionManager.getWorld(dimId)

	override fun toString() =
		"$dimId;$x;$y;$z"

	companion object {
		fun fromString(str: String): DimBlockPos {
			val split = str.split(';')
			return DimBlockPos(split[0].toInt(), split[1].toInt(), split[2].toInt(), split[3].toInt())
		}
	}
}
