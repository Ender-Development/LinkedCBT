@file:Suppress("NOTHING_TO_INLINE")

package io.enderdev.linkedcbt.util.extensions

import io.enderdev.linkedcbt.client.gui.BaseLinkedGui
import net.minecraft.util.text.TextComponentString
import org.ender_development.catalyx.utils.extensions.translate

inline fun String.component() =
	TextComponentString(this)

inline fun String.guiTranslate(vararg format: Any) =
	"${BaseLinkedGui.TRANSLATION_BASE}$this".translate(format)

// yes, this is cursed, and I love it
inline operator fun String.unaryPlus() =
	component()
