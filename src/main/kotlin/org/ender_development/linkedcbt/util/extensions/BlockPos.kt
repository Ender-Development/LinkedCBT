@file:Suppress("NOTHING_TO_INLINE")

package org.ender_development.linkedcbt.util.extensions

import org.ender_development.linkedcbt.data.DimBlockPos
import net.minecraft.util.math.BlockPos

inline infix fun BlockPos.dim(dimId: Int) =
	DimBlockPos(dimId, this)

