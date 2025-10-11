package io.enderdev.linkedtanks.network

import io.enderdev.linkedtanks.Tags
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.fml.relauncher.Side

internal object PacketHandler {
	val channel: SimpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID)

	fun init() {
		channel.registerMessage(ChannelListPacket.ServerHandler::class.java, ChannelListPacket::class.java, 0, Side.SERVER)
		channel.registerMessage(ChannelListPacket.ClientHandler::class.java, ChannelListPacket::class.java, 0, Side.CLIENT)
	}
}

