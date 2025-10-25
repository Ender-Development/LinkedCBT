package io.enderdev.linkedcbt.command

import io.enderdev.linkedcbt.Tags
import io.enderdev.linkedcbt.util.extensions.reply
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.server.command.CommandTreeBase

internal object LinkedCBTCommand : CommandTreeBase() {
	override fun getName() =
		Tags.MOD_ID

	override fun getAliases() =
		listOf("lcbt")
	
	override fun getUsage(sender: ICommandSender) =
		"see /linkedcbt help"

	init {
		addSubcommand(Help)
		addSubcommand(Version)
		addSubcommand(TanksSubcommand)
		addSubcommand(BatteriesSubcommand)
		addSubcommand(ChestsSubcommand)
	}

	object Help : BaseCommand("help") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String?>) {
			sender.reply("/linkedcbt help - show this text")
			sender.reply("/linkedcbt version - show the ${Tags.MOD_NAME} version")
			sender.reply("/linkedcbt tanks help - show tank management help")
			sender.reply("/linkedcbt batteries help - show battery management help")
			sender.reply("/linkedcbt chests help - show chest management help")
		}
	}

	object Version : BaseCommand("version") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String?>) =
			sender.reply("${Tags.MOD_NAME} version ${Tags.VERSION}", TextFormatting.AQUA)
	}
}
