package io.enderdev.linkedcbt.command

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.data.base.BaseChannelData
import io.enderdev.linkedcbt.data.base.BasePersistentData
import io.enderdev.linkedcbt.tiles.BaseLinkedTile
import io.enderdev.linkedcbt.util.extensions.*
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap
import net.minecraft.block.Block
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.common.UsernameCache
import java.util.*

private typealias ChannelDataProvider = (ICommandSender, String) -> Pair<Int, BaseChannelData<*, *>>?
private typealias ChannelDataProviderDeletedWarn = (ICommandSender, String, Boolean) -> Pair<Int, BaseChannelData<*, *>>?

internal object SharedSubcommands {
	fun <CH_DATA : BaseChannelData<CH_DATA, *>> list(server: MinecraftServer, sender: ICommandSender, args: Array<String>, persistentData: BasePersistentData<CH_DATA, *>, extraData: (CH_DATA) -> ITextComponent?) {
		sender.reply("Channels:")
		persistentData.data.toList().sortedBy { it.first }.forEach { (id, data) ->
			val colour = if(data.deleted) TextFormatting.GRAY else TextFormatting.WHITE
			sender.reply("- ${data.displayName(id)}${if(data.deleted) " (deleted)" else ""}", colour)
			sender.reply("owner: ${data.ownerUsername}; ${data.linkedPositions.size} endpoint${if(data.linkedPositions.size == 1) "" else "s"}", colour)
			extraData(data)?.let { sender.reply(it, colour) }
			sender.reply("")
		}
		val endpoints = persistentData.data.map { it.value.linkedPositions.size }.sum()
		sender.reply("Total: ${persistentData.data.count { !it.value.deleted }} (${persistentData.data.size}) channels with $endpoints total endpoint${if(endpoints == 1) "" else "s"}")
	}

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

	abstract class PurgeSubcommand(val baseCommand: String, val channelDataProvider: ChannelDataProviderDeletedWarn, val persistentData: BasePersistentData<*, *>, val block: Block) : BaseCommand("purge") {
		// channelId to milliseconds
		val channelIdConfirmations = Int2LongArrayMap(2).apply {
			defaultReturnValue(0L)
		}

		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
			if(args.isEmpty()) {
				sender.replyFail("Usage: $baseCommand $name <channel id>")
				return
			}

			val (channelId, channel) = channelDataProvider(sender, args[0], false) ?: return

			if(channel.linkedPositions.isNotEmpty() && System.currentTimeMillis() - channelIdConfirmations[channelId] > CONFIRMATION_SECONDS * 1000L) {
				sender.replyWarn("Channel $channelId still has ${channel.linkedPositions.size} endpoint${if(channel.linkedPositions.size == 1) "" else "s"}")
				sender.replyWarn(+"This can happen due to a " + TextComponentTranslation("${block.translationKey}.name") + +" being in an unloaded chunk, and it might relink to the next created channel with id $channelId")
				sender.replyWarn("If you're sure you want to purge this channel, type this command again within $CONFIRMATION_SECONDS seconds")
				channelIdConfirmations.put(channelId, System.currentTimeMillis())
				return
			}

			persistentData.data.remove(channelId)

			sender.reply("Channel $channelId and all of its associated data has been purged, and its channel id is free to be reused")
		}

		companion object {
			private const val CONFIRMATION_SECONDS = 5
		}
	}
}
