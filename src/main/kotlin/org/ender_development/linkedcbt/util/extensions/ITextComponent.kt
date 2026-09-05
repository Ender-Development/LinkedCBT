@file:Suppress("NOTHING_TO_INLINE")

package org.ender_development.linkedcbt.util.extensions

import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextFormatting

inline operator fun ITextComponent.plus(other: ITextComponent): ITextComponent =
	appendSibling(other)

inline fun ITextComponent.withColour(colour: TextFormatting): ITextComponent =
	setStyle(Style().setColor(colour))
