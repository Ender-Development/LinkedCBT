@file:Suppress("NOTHING_TO_INLINE")

package io.enderdev.linkedtanks.util.extensions

import net.minecraft.command.ICommandSender
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextFormatting

inline fun ICommandSender.reply(component: ITextComponent) =
	sendMessage(component)

inline fun ICommandSender.reply(text: String) =
	reply(text.component())

inline fun ICommandSender.reply(text: String, colour: TextFormatting) =
	sendMessage(text.component().withColour(colour))

inline fun ICommandSender.replyFail(text: String) =
	reply(text, TextFormatting.RED)

inline fun ICommandSender.replyWarn(text: String) =
	reply(text, TextFormatting.YELLOW)

inline fun ICommandSender.replyWarn(component: ITextComponent) =
	reply(component.withColour(TextFormatting.YELLOW))
