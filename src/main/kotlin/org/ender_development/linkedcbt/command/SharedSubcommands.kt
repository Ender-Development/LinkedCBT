package org.ender_development.linkedcbt.command

import org.ender_development.linkedcbt.LinkedCBT
import org.ender_development.linkedcbt.data.base.BaseChannelData
import org.ender_development.linkedcbt.data.base.BasePersistentData
import org.ender_development.linkedcbt.tiles.BaseLinkedTile
import org.ender_development.linkedcbt.util.extensions.*
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.ITextComponent
import net.minecraftforge.common.UsernameCache
import org.ender_development.linkedcbt.network.PacketHandler.channel
import java.util.*

internal object SharedSubcommands {
	fun <CH_DATA : BaseChannelData<CH_DATA, *>> list(server: MinecraftServer, sender: ICommandSender, args: Array<String>, persistentData: BasePersistentData<CH_DATA, *>, extraData: (CH_DATA) -> ITextComponent?) {
		val showUUIDs = args.any { it.contains("uuid", true) }
		sender.reply("Channels:")
		persistentData.data.entries.sortedByDescending { it.value.creationTime }.forEach { (uuid, data) ->
			sender.reply("- ${data.displayName()} (${if(showUUIDs) "$uuid; " else ""}${data.linkedPositions.size} endpoint${if(data.linkedPositions.size == 1) "" else "s"})")
			sender.reply("owner: ${data.ownerUsername}")
			extraData(data)?.let { sender.reply(it) }
			sender.reply("")
		}
		val endpoints = persistentData.data.values.sumOf { it.linkedPositions.size }
		sender.reply("Total: ${persistentData.data.size} channels with $endpoints total endpoint${if(endpoints == 1) "" else "s"}")
	}

	fun hijack(server: MinecraftServer, sender: ICommandSender, args: Array<String>, baseCommand: String, persistentData: BasePersistentData<*, *>) {
		if(args.isEmpty()) {
			sender.replyFail("Usage: $baseCommand hijack <channel name/UUID> [player]")
			return
		}

		var isPlayerSpecified = false
		val (channelId, channel) = getChannelData(sender, args, persistentData, true) ?: getChannelData(sender, args.copyOfRange(0, args.lastIndex), persistentData)?.also { isPlayerSpecified = true } ?: return

		var playerUUID = (sender as? EntityPlayer)?.uniqueID

		if(isPlayerSpecified) {
			val playerArg = args.last()

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

		channel.ownerUUID = playerUUID
		channel.ownerUsername = UsernameCache.getLastKnownUsername(playerUUID) ?: playerUUID.toString()
		sender.reply("Channel '${channel.displayName()}' ($channelId) ownership changed to ${channel.ownerUsername} (${channel.ownerUUID})")
	}

	fun delete(server: MinecraftServer, sender: ICommandSender, args: Array<String>, baseCommand: String, persistentData: BasePersistentData<*, *>) {
		if(args.isEmpty()) {
			sender.replyFail("Usage: $baseCommand delete <channel name/UUID>")
			return
		}

		val (channelId, channel) = getChannelData(sender, args, persistentData) ?: return

		persistentData.data.remove(channelId)
		persistentData.write()
		sender.reply("Channel '${channel.displayName()}' ($channelId) has been deleted")
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

	fun <CH_DATA : BaseChannelData<CH_DATA, *>> getChannelData(sender: ICommandSender, args: Array<String>, persistentData: BasePersistentData<CH_DATA, *>, shutUp: Boolean = false): Pair<UUID, CH_DATA>? {
		if(args.isEmpty()) {
			if(!shutUp)
				sender.replyFail("No channel name/UUID provided")
			return null
		}

		if(args.size == 1)
			try {
				val uuid = UUID.fromString(args[0])
				val channel = persistentData.data[uuid]
				if(channel == null) {
					if(!shutUp)
						sender.replyFail("Channel with UUID $uuid doesn't exist")
					throw IllegalArgumentException() // try to do a lookup by channel name as well
				}
				return uuid to channel
			} catch(ignored: IllegalArgumentException) {}

		val name = args.joinToString(" ")
		val entry = persistentData.data.entries.firstOrNull { it.value.name.equals(name, true) }
		if(entry == null) {
			if(!shutUp)
				sender.replyFail("No channel with name '$name' was found")
			return null
		}
		return entry.key to entry.value
	}
}
