package io.enderdev.linkedcbt.command

import io.enderdev.linkedcbt.blocks.ModBlocks
import io.enderdev.linkedcbt.command.SharedSubcommands.getChannelId
import io.enderdev.linkedcbt.data.tanks.LTPersistentData
import io.enderdev.linkedcbt.data.tanks.TankChannelData
import io.enderdev.linkedcbt.util.extensions.*
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.server.command.CommandTreeBase

internal object TanksSubcommand : CommandTreeBase() {
	override fun getName() =
		"tanks"

	override fun getUsage(sender: ICommandSender) =
		"See $BASE_COMMAND help"

	// technically only used in CommandBase but doesn't hurt
	override fun getRequiredPermissionLevel() =
		2

	override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
		if(server.isSinglePlayer)
			return true

		return sender !is EntityPlayer || server.playerList.oppedPlayers.getPermissionLevel(sender.gameProfile) >= 2
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
			sender.reply("$BASE_COMMAND list - show tank channel list")
			sender.reply("$BASE_COMMAND hijack <channel id> [player] - change a tank channel's ownership")
			sender.reply("$BASE_COMMAND delete <channel id> - delete a tank channel")
			sender.reply("$BASE_COMMAND restore <channel id> - restore a tank channel")
			sender.reply("$BASE_COMMAND setcontents <channel id> <fluid | empty> <amount> - set a tank channel's contents")
			sender.reply("$BASE_COMMAND revalidate - validate if all tank channels have saved correct tank positions, this may load chunks")
		}
	}

	object List : BaseCommand("list") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String?>) {
			sender.reply("Channels:")
			LTPersistentData.data.toList().sortedBy { it.first }.forEach { (id, data) ->
				val colour = if(data.deleted) TextFormatting.GRAY else TextFormatting.WHITE
				sender.reply("- ${data.displayName(id)}${if(data.deleted) " (deleted)" else ""}", colour)
				sender.reply("owner: ${data.ownerUsername} (uuid: ${data.ownerUUID})", colour)
				sender.reply((+"${data.fluidAmount.formatNumber()} / ${data.fluidCapacity.formatNumber()} mB of " + data.fluid.nameComponent + +"; ${data.linkedPositions.size} endpoint${if(data.linkedPositions.size == 1) "" else "s"}"), colour)
				sender.reply("")
			}
			val endpoints = LTPersistentData.data.map { it.value.linkedPositions.size }.sum()
			sender.reply("Total: ${LTPersistentData.data.size} (${LTPersistentData.data.count { !it.value.deleted }} not deleted) tank channels with $endpoints total endpoint${if(endpoints == 1) "" else "s"}")
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
			if(args.size < 3 && !(args.size == 2 && args[1] == "empty")) {
				sender.replyFail("Usage: $BASE_COMMAND $name <channel id> <fluid | empty> <amount>")
				return
			}

			val (channelId, channel) = getChannelData(sender, args[0]) ?: return

			val fluidName = args[1]
			val fluidAmount = if(args.size == 3) args[2].toIntOrNull() else null

			if(fluidName == "empty" || fluidAmount == 0) {
				sender.reply(+"Channel $channelId emptied (previous contents: ${channel.fluidAmount.formatNumber()} mB " + channel.fluid.nameComponent + +")")
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

			sender.reply(+"Set contents of channel $channelId to ${fluidAmount.formatNumber()} mB " + fluid.nameComponent + +" (previous contents: ${channel.fluidAmount.formatNumber()} mB " + channel.fluid.nameComponent + +")")
			channel.fluid = fluid
			channel.fluidAmount = fluidAmount
		}
	}

	// TODO throw into SharedSubcommands
	object Purge : BaseCommand("purge") {
		// channelId to milliseconds
		val channelIdConfirmations = Int2LongArrayMap(2).apply {
			defaultReturnValue(0L)
		}
		private const val CONFIRMATION_SECONDS = 5

		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
			if(args.isEmpty()) {
				sender.replyFail("Usage: $BASE_COMMAND $name <channel id>")
				return
			}

			val (channelId, channel) = getChannelData(sender, args[0], false) ?: return

			if(channel.linkedPositions.isNotEmpty() && System.currentTimeMillis() - channelIdConfirmations[channelId] > CONFIRMATION_SECONDS * 1000L) {
				sender.replyWarn("Channel $channelId still has ${channel.linkedPositions.size} endpoint${if(channel.linkedPositions.size == 1) "" else "s"}")
				sender.replyWarn(+"This can happen due to a " + TextComponentTranslation("${ModBlocks.linkedTank.translationKey}.name") + +" being in an unloaded chunk, and it might relink to the next created channel with id $channelId")
				sender.replyWarn("If you're sure you want to purge this channel, type this command again within $CONFIRMATION_SECONDS seconds")
				channelIdConfirmations.put(channelId, System.currentTimeMillis())
				return
			}

			LTPersistentData.data.remove(channelId)

			sender.reply("Channel $channelId and all of its associated data has been purged, and its channel id is free to be reused")
		}
	}

	object Revalidate : BaseCommand("revalidate") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.revalidate(server, sender, args, LTPersistentData, "Linked Tank")
	}


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

	private fun getChannelData(sender: ICommandSender, arg: String, deletedWarn: Boolean = true): Pair<Int, TankChannelData>? {
		val channelId = getChannelId(sender, arg) ?: return null
		val channel = LTPersistentData.data.get(channelId) ?: run {
			sender.replyFail("There is no channel with id $channelId")
			return null
		}

		if(channel.deleted && deletedWarn)
			sender.replyWarn("Channel is deleted")

		return channelId to channel
	}

	private const val BASE_COMMAND = "/linkedcbt tanks"
}
