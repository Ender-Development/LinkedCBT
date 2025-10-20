@file:Suppress("NOTHING_TO_INLINE")

package io.enderdev.linkedcbt.util.extensions

import io.enderdev.linkedcbt.data.DimBlockPos
import net.minecraft.util.math.BlockPos

inline infix fun BlockPos.dim(dimId: Int) =
	DimBlockPos(dimId, this)

