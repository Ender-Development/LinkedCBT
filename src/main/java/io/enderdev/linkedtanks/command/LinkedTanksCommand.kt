package io.enderdev.linkedtanks.command

import io.enderdev.linkedtanks.LinkedTanks.formatNumber
import io.enderdev.linkedtanks.Tags
import io.enderdev.linkedtanks.data.LTPersistentData
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.*
import net.minecraftforge.common.UsernameCache
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.server.command.CommandTreeBase
import java.util.*

object LinkedTanksCommand : CommandTreeBase() {
	override fun getName() =
		"linkedtanks"

	override fun getAliases() =
		emptyList<String>()

	override fun getUsage(sender: ICommandSender) =
		"meow"

	init {
		addSubcommand(Help)
		addSubcommand(Version)
		addSubcommand(Channels)
	}

	object Help : CommandBase() {
		override fun getName() =
			"help"

		override fun getUsage(sender: ICommandSender) =
			"$name meow"

		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String?>) {
			sender.reply("/linkedtanks version - show the ${Tags.MOD_NAME} version")
			sender.reply("/linkedtanks channels list - show channel list")
			sender.reply("/linkedtanks channels hijack <channel id> [player] - change a channel's ownership")
			sender.reply("/linkedtanks channels delete <channel id> - delete a channel")
			sender.reply("/linkedtanks channels undelete <channel id> - undelete a channel")
			sender.reply("/linkedtanks setcontents <channel id> <fluid | empty> <amount> - set a channel's contents")
		}
	}

	object Version : CommandBase() {
		override fun getName() =
			"version"

		override fun getUsage(sender: ICommandSender) =
			"$name meow"

		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String?>) =
			sender.reply("${Tags.MOD_NAME} version ${Tags.VERSION}", TextFormatting.BLUE)
	}

	object Channels : CommandTreeBase() {
		override fun getName() =
			"channels"

		override fun getUsage(sender: ICommandSender) =
			"$name meow"

		// technically only used in CommandBase but doesn't hurt
		override fun getRequiredPermissionLevel() = 4

		override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
			if(server.isSinglePlayer)
				return true

			return sender !is EntityPlayer || server.playerList.oppedPlayers.getPermissionLevel(sender.gameProfile) != 0
		}

		init {
			addSubcommand(List)
			addSubcommand(Hijack)
			addSubcommand(Delete)
			addSubcommand(Undelete)
			addSubcommand(SetContents)
		}

		object List : CommandBase() {
			override fun getName() =
				"list"

			override fun getUsage(sender: ICommandSender) =
				"channels $name meow"

			override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String?>) {
				sender.reply("Channels:")
				LTPersistentData.data.toList().sortedBy { it.first }.forEach { (id, data) ->
					sender.reply("- #$id ${data.name}${if(data.deleted) " (deleted)" else ""}")
					sender.reply("owner: ${data.ownerUsername} (uuid: ${data.ownerUUID})")
					sender.sendMessage((+"${data.fluidAmount.formatNumber()} / ${data.fluidCapacity.formatNumber()} mB of ").appendSibling(data.fluid.nameComponent))
					sender.reply("")
				}
				sender.reply("Total: ${LTPersistentData.data.size} (${LTPersistentData.data.count { !it.value.deleted }} not deleted) channels with ${LTPersistentData.data.map { it.value.linkedPositions.size }.sum()} total endpoints")
			}
		}

		object Hijack : CommandBase() {
			override fun getName() =
				"hijack"

			override fun getUsage(sender: ICommandSender) =
				"channels $name meow"

			override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
				if(args.isEmpty()) {
					sender.replyFail("Usage: /linkedtanks hijack <channel id> [player]")
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
						} catch(e: IllegalArgumentException) {}
				}

				if(playerUUID == null) {
					sender.replyFail("No player provided")
					return
				}

				val (channelId, channel) = getChannelData(sender, args[0]) ?: return

				channel.ownerUUID = playerUUID
				channel.ownerUsername = UsernameCache.getLastKnownUsername(playerUUID) ?: playerUUID.toString()
				sender.reply("Channel $channelId ownership changed to ${channel.ownerUsername} (${channel.ownerUUID})")
			}
		}

		object Delete : CommandBase() {
			override fun getName() =
				"delete"

			override fun getUsage(sender: ICommandSender) =
				"channels $name meow"

			override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
				if(args.isEmpty()) {
					sender.replyFail("Usage: /linkedtanks delete <channel id>")
					return
				}

				val (channelId, channel) = getChannelData(sender, args[0], false) ?: return

				if(channel.deleted) {
					sender.replyFail("Channel $channelId is already deleted")
					return
				}

				channel.deleted = true
				sender.reply("Channel $channelId has been deleted")
			}
		}

		object Undelete : CommandBase() {
			override fun getName() =
				"undelete"

			override fun getUsage(sender: ICommandSender) =
				"channels $name meow"

			override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
				if(args.isEmpty()) {
					sender.replyFail("Usage: /linkedtanks undelete <channel id>")
					return
				}

				val (channelId, channel) = getChannelData(sender, args[0], false) ?: return

				if(!channel.deleted) {
					sender.replyFail("Channel $channelId is not deleted")
					return
				}

				channel.deleted = false
				sender.reply("Channel $channelId has been undeleted/restored")
			}
		}

		object SetContents : CommandBase() {
			override fun getName() =
				"setcontents"

			override fun getAliases() =
				listOf("setContents", "set_contents")

			override fun getUsage(sender: ICommandSender) =
				"channels $name meow"

			override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
				if(args.size < 3 && !(args.size == 2 && args[1] == "empty")) {
					sender.replyFail("Usage: /linkedtanks setcontents <channel id> <fluid | empty> <amount>")
					return
				}

				val (channelId, channel) = getChannelData(sender, args[0]) ?: return

				val fluidName = args[1]
				val fluidAmount = if(args.size == 3) args[2].toIntOrNull() else null

				if(fluidName == "empty" || fluidAmount == 0) {
					sender.reply((+"Channel $channelId emptied (previous contents: ${channel.fluidAmount.formatNumber()} ").appendSibling(channel.fluid.nameComponent).appendSibling(+")"))
					channel.fluidAmount = 0
					channel.fluid = null
					return
				}

				val fluid = FluidRegistry.getFluid(fluidName)
				if(fluid == null) {
					sender.replyFail("Couldn't find any fluid called '$fluidName'")
					return
				}

				if(fluidAmount == null || fluidAmount < 0) {
					sender.replyFail("Couldn't convert '${args[2]}' to a valid number")
					return
				}

				if(fluidAmount > channel.fluidCapacity)
					sender.replyWarn("Setting fluid amount to more than the expected capacity, things might not work as intended")

				sender.reply((+"Set contents of channel $channelId to ${fluidAmount.formatNumber()} mB ").appendSibling(fluid.nameComponent).appendSibling(+" (previous contents: ${channel.fluidAmount.formatNumber()} mB ").appendSibling(channel.fluid.nameComponent).appendSibling(+")"))
				channel.fluid = fluid
				channel.fluidAmount = fluidAmount
			}
		}
	}

	fun getChannelId(sender: ICommandSender, arg: String) =
		arg.toIntOrNull().also {
			if(it == null)
				sender.replyFail("Couldn't convert '$arg' to a numerical channel id")
		}

	fun getChannelData(sender: ICommandSender, arg: String, deletedWarn: Boolean = true): Pair<Int, LTPersistentData.ChannelData>? {
		val channelId = getChannelId(sender, arg) ?: return null
		val channel = LTPersistentData.data.get(channelId) ?: run {
			sender.replyFail("There is no channel with id $channelId")
			return null
		}

		if(channel.deleted && deletedWarn)
			sender.replyWarn("Channel is deleted")

		return channelId to channel
	}

	@Suppress("NOTHING_TO_INLINE")
	private inline fun ICommandSender.reply(component: ITextComponent) =
		sendMessage(component)

	@Suppress("NOTHING_TO_INLINE")
	private inline fun ICommandSender.reply(text: String) =
		reply(+text)

	@Suppress("NOTHING_TO_INLINE")
	private inline fun ICommandSender.reply(text: String, colour: TextFormatting) =
		sendMessage((+text).setStyle(Style().setColor(colour)))

	@Suppress("NOTHING_TO_INLINE")
	private inline fun ICommandSender.replyFail(text: String) =
		reply(text, TextFormatting.RED)

	@Suppress("NOTHING_TO_INLINE")
	private inline fun ICommandSender.replyWarn(text: String) =
		reply(text, TextFormatting.YELLOW)

	@Suppress("NOTHING_TO_INLINE")
	private inline val Fluid?.nameComponent
		get() = if(this == null) +"<empty>" else TextComponentTranslation(realUnlocalisedName)

	// for some reason Fluid#getLocalisedName is overwritten but not this
	private val Fluid.realUnlocalisedName: String
		get() = when(this) {
			FluidRegistry.WATER -> "tile.water.name"
			FluidRegistry.LAVA -> "tile.lava.name"
			else -> unlocalizedName
		}

	// yes, this is cursed, and I love it
	@Suppress("NOTHING_TO_INLINE")
	private inline operator fun String.unaryPlus() =
		TextComponentString(this)
}
