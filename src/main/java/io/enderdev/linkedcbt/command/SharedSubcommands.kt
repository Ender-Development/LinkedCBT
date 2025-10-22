package io.enderdev.linkedcbt.command

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.data.base.BaseChannelData
import io.enderdev.linkedcbt.data.base.BasePersistentData
import io.enderdev.linkedcbt.tiles.BaseLinkedTile
import io.enderdev.linkedcbt.util.extensions.reply
import io.enderdev.linkedcbt.util.extensions.replyFail
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraftforge.common.UsernameCache
import java.util.*

private typealias ChannelDataProvider = (ICommandSender, String) -> Pair<Int, BaseChannelData<*, *>>?
private typealias ChannelDataProviderDeletedWarn = (ICommandSender, String, Boolean) -> Pair<Int, BaseChannelData<*, *>>?

object SharedSubcommands {
	fun hijack(server: MinecraftServer, sender: ICommandSender, args: Array<String>, baseCommand: String, channelDataProvider: ChannelDataProvider) {
		if(args.isEmpty()) {
			sender.replyFail("Usage: $baseCommand hijack <channel id> [player]")
			return
		}

		var playerUUID = (sender as? EntityPlayer)?.uniqueID

		if(args.size > 1) {
			val playerArg = args[1]

			playerUUID = UsernameCache.getMap().firstNotNullOfOrNull { (uuid, username) ->
				if(username == playerArg)
					uuid
				else
					null
			}

			if(playerUUID == null)
				try {
					playerUUID = UUID.fromString(playerArg)
				} catch(_: IllegalArgumentException) {}
		}

		if(playerUUID == null) {
			sender.replyFail("No player provided")
			return
		}

		val (channelId, channel) = channelDataProvider(sender, args[0]) ?: return

		channel.ownerUUID = playerUUID
		channel.ownerUsername = UsernameCache.getLastKnownUsername(playerUUID) ?: playerUUID.toString()
		sender.reply("Channel $channelId ownership changed to ${channel.ownerUsername} (${channel.ownerUUID})")
	}

	fun delete(server: MinecraftServer, sender: ICommandSender, args: Array<String>, baseCommand: String, channelDataProvider: ChannelDataProviderDeletedWarn) {
		if(args.isEmpty()) {
			sender.replyFail("Usage: $baseCommand delete <channel id>")
			return
		}

		val (channelId, channel) = channelDataProvider(sender, args[0], false) ?: return

		if(channel.deleted) {
			sender.replyFail("Channel $channelId is already deleted")
			return
		}

		channel.deleted = true
		sender.reply("Channel $channelId has been deleted")
	}

	fun restore(server: MinecraftServer, sender: ICommandSender, args: Array<String>, baseCommand: String, channelDataProvider: ChannelDataProviderDeletedWarn) {
		if(args.isEmpty()) {
			sender.replyFail("Usage: $baseCommand restore <channel id>")
			return
		}

		val (channelId, channel) = channelDataProvider(sender, args[0], false) ?: return

		if(!channel.deleted) {
			sender.replyFail("Channel $channelId is not deleted")
			return
		}

		channel.deleted = false
		sender.reply("Channel $channelId has been restored")
	}

	inline fun <reified TE : BaseLinkedTile<TE, *, *, *>> revalidate(server: MinecraftServer, sender: ICommandSender, args: Array<String>, persistentData: BasePersistentData<*, TE>, name: String) {
		persistentData.data.forEach { (id, data) ->
			data.linkedPositions.removeIf { pos ->
				// idk if this loads dimensions, so if it returns null, just bail
				val world = pos.world ?: return@removeIf false.also {
					LinkedCBT.logger.info("Revalidate: couldn't get dimension with id {} (pos: {}, channel id: {})!", pos.dimId, pos, id)
				}
				val te = world.getTileEntity(pos.pos) as? TE ?: return@removeIf true.also {
					LinkedCBT.logger.info("Revalidate: there was no {} at {}; removing from channel id {}!", name, pos, id)
				}
				return@removeIf (te.channelId != id).also {
					if(it)
						LinkedCBT.logger.info("Revalidate: {} at {} had channelId {} instead of expected {}!", name, pos, te.channelId, id)
				}
			}
		}
		sender.reply("Channels revalidated")
	}

	fun getChannelId(sender: ICommandSender, arg: String) =
		arg.toIntOrNull().also {
			if(it == null)
				sender.replyFail("Couldn't convert '$arg' to a numerical channel id")
		}
}
