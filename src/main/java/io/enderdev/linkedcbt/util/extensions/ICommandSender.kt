@file:Suppress("NOTHING_TO_INLINE")

package io.enderdev.linkedcbt.util.extensions

import net.minecraft.command.ICommandSender
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextFormatting

inline fun ICommandSender.reply(component: ITextComponent) =
	sendMessage(component)

inline fun ICommandSender.reply(component: ITextComponent, colour: TextFormatting) =
	reply(component.withColour(colour))

inline fun ICommandSender.reply(text: String) =
	reply(text.component())

inline fun ICommandSender.reply(text: String, colour: TextFormatting) =
	reply(text.component(), colour)

inline fun ICommandSender.replyFail(text: String) =
	reply(text, TextFormatting.RED)

inline fun ICommandSender.replyFail(component: ITextComponent) =
	reply(component, TextFormatting.RED)

inline fun ICommandSender.replyWarn(text: String) =
	reply(text, TextFormatting.YELLOW)

inline fun ICommandSender.replyWarn(component: ITextComponent) =
	reply(component, TextFormatting.YELLOW)
