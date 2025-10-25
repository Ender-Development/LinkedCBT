package io.enderdev.linkedcbt.command

import io.enderdev.linkedcbt.blocks.ModBlocks
import io.enderdev.linkedcbt.command.SharedSubcommands.getChannelId
import io.enderdev.linkedcbt.data.tanks.LTPersistentData
import io.enderdev.linkedcbt.data.tanks.TankChannelData
import io.enderdev.linkedcbt.util.extensions.*
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentTranslation
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
			sender.reply("$BASE_COMMAND list - show tank channel list")
			sender.reply("$BASE_COMMAND hijack <channel id> [player] - change a tank channel's ownership")
			sender.reply("$BASE_COMMAND delete <channel id> - delete a tank channel")
			sender.reply("$BASE_COMMAND restore <channel id> - restore a tank channel")
			sender.reply("$BASE_COMMAND setcontents <channel id> <fluid | empty> <amount> - set a tank channel's contents")
			sender.reply("$BASE_COMMAND revalidate - validate if all tank channels have saved correct tank positions, this may load chunks")
		}
	}

	object List : BaseCommand("list") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.list(server, sender, args, LTPersistentData) { data ->
				+"${data.fluidAmount.formatNumber()} / ${data.fluidCapacity.formatNumber()} mB of " + data.fluid.nameComponent
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

	object Purge : SharedSubcommands.PurgeSubcommand(BASE_COMMAND, ::getChannelData, LTPersistentData, ModBlocks.linkedTank)

	object Revalidate : BaseCommand("revalidate") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.revalidate(server, sender, args, LTPersistentData, "Linked Tank")
	}

	private val Fluid?.nameComponent
		inline get() = if(this == null) +"<empty>" else TextComponentTranslation(realUnlocalisedName)

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
