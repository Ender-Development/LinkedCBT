package io.enderdev.linkedtanks.util.extensions

import io.enderdev.linkedtanks.data.DimBlockPos
import net.minecraft.util.math.BlockPos

@Suppress("NOTHING_TO_INLINE")
inline infix fun BlockPos.dim(dimId: Int) =
	DimBlockPos(dimId, this)

