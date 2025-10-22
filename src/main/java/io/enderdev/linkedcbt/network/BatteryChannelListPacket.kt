package io.enderdev.linkedcbt.network

import io.enderdev.linkedcbt.data.batteries.BatteryChannelData
import io.enderdev.linkedcbt.data.batteries.LBPersistentData
import io.enderdev.linkedcbt.data.batteries.client.ClientBatteryChannelData
import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import org.ender_development.catalyx.utils.extensions.readString
import org.ender_development.catalyx.utils.extensions.writeString
import kotlin.random.Random

class BatteryChannelListPacket : BaseChannelListPacket<BatteryChannelData, ClientBatteryChannelData> {
	override fun writeChannel(buf: ByteBuf, channel: ClientBatteryChannelData) {
		buf.writeInt(channel.id)
		buf.writeString(channel.name)
		buf.writeInt(channel.energyAmount)
		buf.writeInt(channel.energyCapacity)
	}

	override fun readChannel(buf: ByteBuf) =
		ClientBatteryChannelData(buf.readInt(), buf.readString(), buf.readInt(), buf.readInt())

	constructor(id: Int) {
		this.id = id
	}

	constructor() : this(Random.nextInt())

	class ServerHandler() : BaseChannelListPacket.ServerHandler<BatteryChannelData, ClientBatteryChannelData, BatteryChannelListPacket>(LBPersistentData)
	class ClientHandler() : BaseChannelListPacket.ClientHandler<BatteryChannelData, ClientBatteryChannelData, BatteryChannelListPacket>(handlers)

	companion object {
		/**
		 * Client-side only
		 */
		@JvmStatic
		val handlers = Int2ObjectArrayMap<(response: BatteryChannelListPacket) -> Unit>(2)
	}
}
