package org.ender_development.linkedcbt.command

import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraftforge.server.command.CommandTreeBase
import org.ender_development.linkedcbt.data.chests.LCPersistentData
import org.ender_development.linkedcbt.util.extensions.reply

internal object ChestsSubcommand : CommandTreeBase() {
	override fun getName() =
		"chests"

	override fun getUsage(sender: ICommandSender) =
		"See $BASE_COMMAND help"

	// technically only used in CommandBase but doesn't hurt
	override fun getRequiredPermissionLevel() =
		2

	override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
		return server.isSinglePlayer || sender !is EntityPlayer || server.playerList.oppedPlayers.getPermissionLevel(sender.gameProfile) >= requiredPermissionLevel
	}

	init {
		addSubcommand(Help)
		addSubcommand(List)
		addSubcommand(Hijack)
		addSubcommand(Delete)
		addSubcommand(Revalidate)
	}

	object Help : BaseCommand("help") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String?>) {
			sender.reply("$BASE_COMMAND list - show chest channel list")
			sender.reply("$BASE_COMMAND hijack <channel name/UUID> [player] - change a chest channel's ownership")
			sender.reply("$BASE_COMMAND delete <channel name/UUID> - delete a chest channel")
			sender.reply("$BASE_COMMAND revalidate - validate if all chest channels have saved correct chest positions, this may load chunks")
		}
	}

	object List : BaseCommand("list") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.list(server, sender, args, LCPersistentData) { null }
	}

	object Hijack : BaseCommand("hijack") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.hijack(server, sender, args, BASE_COMMAND, LCPersistentData)
	}

	object Delete : BaseCommand("delete") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.delete(server, sender, args, BASE_COMMAND, LCPersistentData)
	}

	object Revalidate : BaseCommand("revalidate") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.revalidate(server, sender, args, LCPersistentData, "Linked Chest")
	}

	private const val BASE_COMMAND = "/linkedcbt chests"
}
