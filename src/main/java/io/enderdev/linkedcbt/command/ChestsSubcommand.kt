package io.enderdev.linkedcbt.command

import io.enderdev.linkedcbt.blocks.ModBlocks
import io.enderdev.linkedcbt.command.SharedSubcommands.getChannelId
import io.enderdev.linkedcbt.data.chests.ChestChannelData
import io.enderdev.linkedcbt.data.chests.LCPersistentData
import io.enderdev.linkedcbt.util.extensions.reply
import io.enderdev.linkedcbt.util.extensions.replyFail
import io.enderdev.linkedcbt.util.extensions.replyWarn
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraftforge.server.command.CommandTreeBase

internal object ChestsSubcommand : CommandTreeBase() {
	override fun getName() =
		"chests"

	override fun getUsage(sender: ICommandSender) =
		"See $BASE_COMMAND help"

	// technically only used in CommandBase but doesn't hurt
	override fun getRequiredPermissionLevel() =
		2

	override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
		if(server.isSinglePlayer)
			return true

		return sender !is EntityPlayer || server.playerList.oppedPlayers.getPermissionLevel(sender.gameProfile) >= requiredPermissionLevel
	}

	init {
		addSubcommand(Help)
		addSubcommand(List)
		addSubcommand(Hijack)
		addSubcommand(Delete)
		addSubcommand(Restore)
		addSubcommand(Purge)
		addSubcommand(Revalidate)
	}

	object Help : BaseCommand("help") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String?>) {
			sender.reply("$BASE_COMMAND list - show chest channel list")
			sender.reply("$BASE_COMMAND hijack <channel id> [player] - change a chest channel's ownership")
			sender.reply("$BASE_COMMAND delete <channel id> - delete a chest channel")
			sender.reply("$BASE_COMMAND restore <channel id> - restore a chest channel")
			sender.reply("$BASE_COMMAND revalidate - validate if all chest channels have saved correct chest positions, this may load chunks")
		}
	}

	object List : BaseCommand("list") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.list(server, sender, args, LCPersistentData) { null }
	}

	object Hijack : BaseCommand("hijack") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.hijack(server, sender, args, BASE_COMMAND, ::getChannelData)
	}

	object Delete : BaseCommand("delete") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.delete(server, sender, args, BASE_COMMAND, ::getChannelData)
	}

	object Restore : BaseCommand("restore") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.restore(server, sender, args, BASE_COMMAND, ::getChannelData)
	}

	object Purge : SharedSubcommands.PurgeSubcommand(BASE_COMMAND, ::getChannelData, LCPersistentData, ModBlocks.linkedChest)

	object Revalidate : BaseCommand("revalidate") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.revalidate(server, sender, args, LCPersistentData, "Linked Chest")
	}

	private fun getChannelData(sender: ICommandSender, arg: String, deletedWarn: Boolean = true): Pair<Int, ChestChannelData>? {
		val channelId = getChannelId(sender, arg) ?: return null
		val channel = LCPersistentData.data.get(channelId) ?: run {
			sender.replyFail("There is no channel with id $channelId")
			return null
		}

		if(channel.deleted && deletedWarn)
			sender.replyWarn("Channel is deleted")

		return channelId to channel
	}

	private const val BASE_COMMAND = "/linkedcbt chests"
}
