package io.enderdev.linkedcbt.util.extensions

import io.enderdev.linkedcbt.LinkedCBT

@Suppress("NOTHING_TO_INLINE")
inline fun Int.formatNumber(): String =
	LinkedCBT.numberFormat.format(this)
