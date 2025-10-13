@file:Suppress("NOTHING_TO_INLINE")

package io.enderdev.linkedtanks.util.extensions

import net.minecraft.util.text.TextComponentString

fun String.component() =
	TextComponentString(this)
