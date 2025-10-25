package io.enderdev.linkedcbt.network

import io.enderdev.linkedcbt.data.chests.ChestChannelData
import io.enderdev.linkedcbt.data.chests.LCPersistentData
import io.enderdev.linkedcbt.data.chests.client.ClientChestChannelData
import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import org.ender_development.catalyx.utils.extensions.readString
import org.ender_development.catalyx.utils.extensions.writeString
import kotlin.random.Random

class ChestChannelListPacket : BaseChannelListPacket<ChestChannelData, ClientChestChannelData> {
	override fun writeChannel(buf: ByteBuf, channel: ClientChestChannelData) {
		buf.writeInt(channel.id)
		buf.writeString(channel.name)
	}

	override fun readChannel(buf: ByteBuf) =
		ClientChestChannelData(buf.readInt(), buf.readString(), emptyArray())

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
