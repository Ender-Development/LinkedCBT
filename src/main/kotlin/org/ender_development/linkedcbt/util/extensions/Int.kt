package org.ender_development.linkedcbt.util.extensions

import org.ender_development.linkedcbt.LinkedCBT

@Suppress("NOTHING_TO_INLINE")
inline fun Int.formatNumber(): String =
	LinkedCBT.numberFormat.format(this)
