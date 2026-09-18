package org.ender_development.linkedcbt.network

import org.ender_development.linkedcbt.data.chests.ChestChannelData
import org.ender_development.linkedcbt.data.chests.LCPersistentData
import org.ender_development.linkedcbt.data.chests.client.ClientChestChannelData
import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import org.ender_development.catalyx.api.v1.common.extensions.readString
import org.ender_development.catalyx.api.v1.common.extensions.readUUID
import org.ender_development.catalyx.api.v1.common.extensions.writeString
import org.ender_development.catalyx.api.v1.common.extensions.writeUUID
import kotlin.random.Random

class ChestChannelListPacket : BaseChannelListPacket<ChestChannelData, ClientChestChannelData> {
	override fun writeChannel(buf: ByteBuf, channel: ClientChestChannelData) {
		buf.writeUUID(channel.id)
		buf.writeString(channel.name)
		buf.writeLong(channel.creationTime)
	}

	override fun readChannel(buf: ByteBuf) =
		ClientChestChannelData(buf.readUUID(), buf.readString(), buf.readLong())

	constructor(id: Int) {
		this.id = id
	}

	constructor() : this(Random.nextInt())

	class ServerHandler() : BaseChannelListPacket.ServerHandler<ChestChannelData, ClientChestChannelData, ChestChannelListPacket>(LCPersistentData)
	class ClientHandler() : BaseChannelListPacket.ClientHandler<ChestChannelData, ClientChestChannelData, ChestChannelListPacket>(handlers)

	companion object {
		/**
		 * Client-side only
		 */
		@JvmStatic
		val handlers = Int2ObjectArrayMap<(response: ChestChannelListPacket) -> Unit>(2)
	}
}
