package io.enderdev.linkedtanks.command

import io.enderdev.linkedtanks.data.LTPersistentData
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentString

object LinkedTanksCommand : CommandBase() {
	override fun getName() =
		"linkedtanks"

	override fun getUsage(sender: ICommandSender) = "TODO"

	override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
		println(args)
		if(args.isNotEmpty() && args[0].trim() == "channels") {
			println("nyaa")
			sender.sendMessage(TextComponentString("Channels:"))
			LTPersistentData.data.forEach { (id, data) ->
				sender.sendMessage(TextComponentString("- $id:"))
				sender.sendMessage(TextComponentString("$data"))
			}
		}
	}
}
