package org.ender_development.linkedcbt.command

import org.ender_development.linkedcbt.util.extensions.reply
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.server.command.CommandTreeBase
import org.ender_development.linkedcbt.Reference

internal object LinkedCBTCommand : CommandTreeBase() {
	override fun getName() =
		Reference.MODID

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
			sender.reply("/linkedcbt version - show the ${Reference.MOD_NAME} version")
			sender.reply("/linkedcbt tanks help - show tank management help")
			sender.reply("/linkedcbt batteries help - show battery management help")
			sender.reply("/linkedcbt chests help - show chest management help")
		}
	}

	object Version : BaseCommand("version") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String?>) =
			sender.reply("${Reference.MOD_NAME} version ${Reference.VERSION}", TextFormatting.AQUA)
	}
}
