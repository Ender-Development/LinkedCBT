package io.enderdev.linkedtanks.command

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer

class LinkedTanksCommand : CommandBase() {
	override fun getName() =
		"linkedtanks"

	override fun getUsage(sender: ICommandSender) = "TODO"

	override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
		TODO()
	}
}
