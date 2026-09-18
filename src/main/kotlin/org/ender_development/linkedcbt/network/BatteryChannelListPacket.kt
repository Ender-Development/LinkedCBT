package org.ender_development.linkedcbt.network

import org.ender_development.linkedcbt.data.batteries.BatteryChannelData
import org.ender_development.linkedcbt.data.batteries.LBPersistentData
import org.ender_development.linkedcbt.data.batteries.client.ClientBatteryChannelData
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import net.minecraftforge.fml.common.network.ByteBufUtils
import org.ender_development.catalyx.api.v1.common.extensions.readString
import org.ender_development.catalyx.api.v1.common.extensions.readUUID
import org.ender_development.catalyx.api.v1.common.extensions.writeString
import org.ender_development.catalyx.api.v1.common.extensions.writeUUID
import kotlin.random.Random

class BatteryChannelListPacket : BaseChannelListPacket<BatteryChannelData, ClientBatteryChannelData> {
	override fun writeChannel(buf: ByteBuf, channel: ClientBatteryChannelData) {
		buf.writeUUID(channel.id)
		buf.writeString(channel.name)
		buf.writeLong(channel.creationTime)
	}

	override fun readChannel(buf: ByteBuf) =
		ClientBatteryChannelData(buf.readUUID(), buf.readString(), buf.readLong())

	constructor(id: Int) {
		this.id = id
	}

	constructor() : this(Random.nextInt())

	class ServerHandler : BaseChannelListPacket.ServerHandler<BatteryChannelData, ClientBatteryChannelData, BatteryChannelListPacket>(LBPersistentData)
	class ClientHandler : BaseChannelListPacket.ClientHandler<BatteryChannelData, ClientBatteryChannelData, BatteryChannelListPacket>(handlers)

	companion object {
		/**
		 * Client-side only
		 */
		@JvmStatic
		val handlers = Int2ObjectArrayMap<(response: BatteryChannelListPacket) -> Unit>(2)
	}
}
