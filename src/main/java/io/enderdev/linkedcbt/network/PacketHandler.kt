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
	}
}

