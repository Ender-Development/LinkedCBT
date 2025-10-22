package io.enderdev.linkedcbt.command

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

internal abstract class BaseCommand(@get:JvmName("getCommandName") val name: String) : CommandBase() {
	override fun getName() =
		name

	override fun getUsage(sender: ICommandSender) =
		"See /lcbt help"
}
