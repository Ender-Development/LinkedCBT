package io.enderdev.linkedcbt.network

import io.enderdev.linkedcbt.Tags
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.fml.relauncher.Side

internal object PacketHandler {
	val channel: SimpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID)

	fun init() {
		channel.registerMessage(TankChannelListPacket.ServerHandler::class.java, TankChannelListPacket::class.java, 0, Side.SERVER)
		channel.registerMessage(TankChannelListPacket.ClientHandler::class.java, TankChannelListPacket::class.java, 0, Side.CLIENT)
		channel.registerMessage(BatteryChannelListPacket.ServerHandler::class.java, BatteryChannelListPacket::class.java, 1, Side.SERVER)
		channel.registerMessage(BatteryChannelListPacket.ClientHandler::class.java, BatteryChannelListPacket::class.java, 1, Side.CLIENT)
		channel.registerMessage(ChestChannelListPacket.ServerHandler::class.java, ChestChannelListPacket::class.java, 2, Side.SERVER)
		channel.registerMessage(ChestChannelListPacket.ClientHandler::class.java, ChestChannelListPacket::class.java, 2, Side.CLIENT)
	}
}

