package org.ender_development.linkedcbt.command

import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.server.command.CommandTreeBase
import org.ender_development.linkedcbt.command.SharedSubcommands.getChannelData
import org.ender_development.linkedcbt.data.tanks.LTPersistentData
import org.ender_development.linkedcbt.util.extensions.*

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
		addSubcommand(SetContents)
		addSubcommand(Revalidate)
	}

	object Help : BaseCommand("help") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String?>) {
			sender.reply("$BASE_COMMAND list - show tank channel list")
			sender.reply("$BASE_COMMAND hijack <channel name/UUID> [player] - change a tank channel's ownership")
			sender.reply("$BASE_COMMAND delete <channel name/UUID> - delete a tank channel")
			sender.reply("$BASE_COMMAND setcontents <channel name/UUID> <fluid | empty> <amount> - set a tank channel's contents")
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
			SharedSubcommands.hijack(server, sender, args, BASE_COMMAND, LTPersistentData)
	}

	object Delete : BaseCommand("delete") {
		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
			SharedSubcommands.delete(server, sender, args, BASE_COMMAND, LTPersistentData)
	}

	object SetContents : BaseCommand("setcontents") {
		override fun getAliases() =
			listOf("setContents", "set_contents")

		override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
			if(args.size < 3 && !(args.size == 2 && args[1] == "empty")) {
				sender.replyFail("Usage: $BASE_COMMAND $name <channel name/UUID> <fluid | empty> <amount>")
				return
			}

			val fluidAmount: Int
			val fluidName: String
			val supposedChannelName: Array<String>

			val last = args.last()
			if(last.equals("empty", true)) {
				fluidAmount = 0
				fluidName = ""
				supposedChannelName = args.copyOfRange(0, args.lastIndex)
			} else if(last.all(Char::isDigit)) {
				fluidAmount = last.toInt()
				if(fluidAmount < 0) {
					sender.replyFail("Invalid fluid amount - $fluidAmount")
					return
				}
				fluidName = args[args.lastIndex - 1]
				supposedChannelName = if(fluidAmount == 0 && FluidRegistry.getFluid(fluidName) == null)
					args.copyOfRange(0, args.lastIndex)
				else
					args.copyOfRange(0, args.lastIndex - 1)
			} else {
				sender.replyFail("Usage: $BASE_COMMAND $name <channel name/UUID. <fluid | empty> <amount>")
				return
			}

			val (channelId, channel) = getChannelData(sender, supposedChannelName, LTPersistentData) ?: return

			if(fluidAmount == 0) {
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

			if(fluidAmount > channel.fluidCapacity)
				sender.replyWarn("Setting fluid amount to more than the expected capacity, things might not work as intended")

			sender.reply(+"Set contents of channel $channelId to ${fluidAmount.formatNumber()} mB " + fluid.nameComponent + +" (previous contents: ${channel.fluidAmount.formatNumber()} mB " + channel.fluid.nameComponent + +")")
			channel.fluid = fluid
			channel.fluidAmount = fluidAmount
		}
	}

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

	private const val BASE_COMMAND = "/linkedcbt tanks"
}
