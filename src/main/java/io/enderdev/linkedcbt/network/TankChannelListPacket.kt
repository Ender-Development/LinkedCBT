package io.enderdev.linkedcbt.network

import io.enderdev.linkedcbt.data.tanks.LTPersistentData
import io.enderdev.linkedcbt.data.tanks.TankChannelData
import io.enderdev.linkedcbt.data.tanks.client.ClientTankChannelData
import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import org.ender_development.catalyx.utils.extensions.readString
import org.ender_development.catalyx.utils.extensions.writeString
import kotlin.random.Random

class TankChannelListPacket : BaseChannelListPacket<TankChannelData, ClientTankChannelData> {
	override fun writeChannel(buf: ByteBuf, channel: ClientTankChannelData) {
		buf.writeInt(channel.id)
		buf.writeString(channel.name)
	}

	override fun readChannel(buf: ByteBuf) =
		ClientTankChannelData(buf.readInt(), buf.readString())

	constructor(id: Int) {
		this.id = id
	}

	constructor() : this(Random.nextInt())

	class ServerHandler() : BaseChannelListPacket.ServerHandler<TankChannelData, ClientTankChannelData, TankChannelListPacket>(LTPersistentData)
	class ClientHandler() : BaseChannelListPacket.ClientHandler<TankChannelData, ClientTankChannelData, TankChannelListPacket>(handlers)

	companion object {
		/**
		 * Client-side only
		 */
		@JvmStatic
		val handlers = Int2ObjectArrayMap<(response: TankChannelListPacket) -> Unit>(2)
	}
}
