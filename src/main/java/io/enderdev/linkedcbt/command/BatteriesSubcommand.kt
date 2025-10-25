package io.enderdev.linkedcbt.command

import io.enderdev.linkedcbt.blocks.ModBlocks
import io.enderdev.linkedcbt.command.SharedSubcommands.getChannelId
import io.enderdev.linkedcbt.data.batteries.BatteryChannelData
import io.enderdev.linkedcbt.data.batteries.LBPersistentData
import io.enderdev.linkedcbt.util.extensions.*
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraftforge.server.command.CommandTreeBase

internal object BatteriesSubcommand : CommandTreeBase() {
	override fun getName() =
		"batteries"

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
		addSubcommand(SetContents)
		addSubcommand(Purge)
		addSubcommand(Revalidate)
	}

	object Help : BaseCommand("help") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String?>) {
			sender.reply("$BASE_COMMAND list - show battery channel list")
			sender.reply("$BASE_COMMAND hijack <channel id> [player] - change a battery channel's ownership")
			sender.reply("$BASE_COMMAND delete <channel id> - delete a battery channel")
			sender.reply("$BASE_COMMAND restore <channel id> - restore a battery channel")
			sender.reply("$BASE_COMMAND setcontents <channel id> <amount> - set a battery channel's energy amount")
			sender.reply("$BASE_COMMAND revalidate - validate if all battery channels have saved correct battery positions, this may load chunks")
		}
	}

	object List : BaseCommand("list") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.list(server, sender, args, LBPersistentData) { data ->
				+"${data.energyAmount.formatNumber()} / ${data.energyCapacity.formatNumber()} FE"
			}
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

	object SetContents : BaseCommand("setcontents") {
		override fun getAliases() =
			listOf("setContents", "set_contents")

		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
			if(args.size < 2) {
				sender.replyFail("Usage: $BASE_COMMAND $name <channel id> <amount>")
				return
			}

			val (channelId, channel) = getChannelData(sender, args[0]) ?: return

			val energyAmount = args[1].toIntOrNull()
			if(energyAmount == null || energyAmount < 0) {
				sender.replyFail("Couldn't convert '${args[1]}' to a valid number")
				return
			}

			if(energyAmount > channel.energyCapacity)
				sender.replyWarn("Setting energy amount to more than the expected capacity, things might not work as intended")

			sender.reply(+"Set contents of channel $channelId to ${energyAmount.formatNumber()} FE (previous contents: ${channel.energyAmount.formatNumber()} FE)")
			channel.energyAmount = energyAmount
		}
	}

	object Purge : SharedSubcommands.PurgeSubcommand(BASE_COMMAND, ::getChannelData, LBPersistentData, ModBlocks.linkedBattery)

	object Revalidate : BaseCommand("revalidate") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.revalidate(server, sender, args, LBPersistentData, "Linked Battery")
	}

	private fun getChannelData(sender: ICommandSender, arg: String, deletedWarn: Boolean = true): Pair<Int, BatteryChannelData>? {
		val channelId = getChannelId(sender, arg) ?: return null
		val channel = LBPersistentData.data.get(channelId) ?: run {
			sender.replyFail("There is no channel with id $channelId")
			return null
		}

		if(channel.deleted && deletedWarn)
			sender.replyWarn("Channel is deleted")

		return channelId to channel
	}

	private const val BASE_COMMAND = "/linkedcbt batteries"
}
