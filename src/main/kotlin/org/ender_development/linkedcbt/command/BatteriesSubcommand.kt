package org.ender_development.linkedcbt.command

import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraftforge.server.command.CommandTreeBase
import org.ender_development.linkedcbt.command.SharedSubcommands.getChannelData
import org.ender_development.linkedcbt.data.batteries.LBPersistentData
import org.ender_development.linkedcbt.util.extensions.*

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
		addSubcommand(SetContents)
		addSubcommand(Revalidate)
	}

	object Help : BaseCommand("help") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String?>) {
			sender.reply("$BASE_COMMAND list - show battery channel list")
			sender.reply("$BASE_COMMAND hijack <channel name/UUID> [player] - change a battery channel's ownership")
			sender.reply("$BASE_COMMAND delete <channel name/UUID> - delete a battery channel")
			sender.reply("$BASE_COMMAND setcontents <channel name/UUID> <amount> - set a battery channel's energy amount")
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
			SharedSubcommands.hijack(server, sender, args, BASE_COMMAND, LBPersistentData)
	}

	object Delete : BaseCommand("delete") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.delete(server, sender, args, BASE_COMMAND, LBPersistentData)
	}

	object SetContents : BaseCommand("setcontents") {
		override fun getAliases() =
			listOf("setContents", "set_contents")

		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
			if(args.size < 2) {
				sender.replyFail("Usage: $BASE_COMMAND $name <channel name/UUID> <amount>")
				return
			}

			val energyAmount = args.last().toIntOrNull()
			if(energyAmount == null || energyAmount < 0) {
				sender.replyFail("Couldn't convert '${args.last()}' to a valid number")
				return
			}

			val (channelId, channel) = getChannelData(sender, args.copyOfRange(0, args.lastIndex), LBPersistentData) ?: return

			if(energyAmount > channel.energyCapacity)
				sender.replyWarn("Setting energy amount to more than the expected capacity, things might not work as intended")

			sender.reply(+"Set contents of channel $channelId to ${energyAmount.formatNumber()} FE (previous contents: ${channel.energyAmount.formatNumber()} FE)")
			channel.energyAmount = energyAmount
		}
	}

	object Revalidate : BaseCommand("revalidate") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.revalidate(server, sender, args, LBPersistentData, "Linked Battery")
	}

	private const val BASE_COMMAND = "/linkedcbt batteries"
}
