package org.ender_development.linkedcbt.network

import org.ender_development.linkedcbt.data.tanks.LTPersistentData
import org.ender_development.linkedcbt.data.tanks.TankChannelData
import org.ender_development.linkedcbt.data.tanks.client.ClientTankChannelData
import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import org.ender_development.catalyx.api.v1.common.extensions.readString
import org.ender_development.catalyx.api.v1.common.extensions.readUUID
import org.ender_development.catalyx.api.v1.common.extensions.writeString
import org.ender_development.catalyx.api.v1.common.extensions.writeUUID
import kotlin.random.Random

class TankChannelListPacket : BaseChannelListPacket<TankChannelData, ClientTankChannelData> {
	override fun writeChannel(buf: ByteBuf, channel: ClientTankChannelData) {
		buf.writeUUID(channel.id)
		buf.writeString(channel.name)
		buf.writeLong(channel.creationTime)
	}

	override fun readChannel(buf: ByteBuf) =
		ClientTankChannelData(buf.readUUID(), buf.readString(), buf.readLong())

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
