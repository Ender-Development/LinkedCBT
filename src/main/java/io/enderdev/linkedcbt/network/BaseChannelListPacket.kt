package io.enderdev.linkedcbt.network

import io.enderdev.linkedcbt.data.base.BaseChannelData
import io.enderdev.linkedcbt.data.base.BasePersistentData
import io.enderdev.linkedcbt.data.base.client.ClientBaseChannelData
import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import kotlin.random.Random

abstract class BaseChannelListPacket<CH_DATA : BaseChannelData<CH_DATA, CLIENT_CH_DATA>, CLIENT_CH_DATA : ClientBaseChannelData<CLIENT_CH_DATA, CH_DATA>> : IMessage {
	var id: Int
	val channelData = mutableListOf<CLIENT_CH_DATA>()

	override fun toBytes(buf: ByteBuf) {
		buf.writeInt(id)
		buf.writeInt(channelData.size)
		channelData.forEach {
			writeChannel(buf, it)
		}
	}

	protected abstract fun writeChannel(buf: ByteBuf, channel: CLIENT_CH_DATA)

	override fun fromBytes(buf: ByteBuf) {
		id = buf.readInt()
		repeat(buf.readInt()) {
			channelData.add(readChannel(buf))
		}
	}

	protected abstract fun readChannel(buf: ByteBuf): CLIENT_CH_DATA

	constructor(id: Int) {
		this.id = id
	}

	constructor() : this(Random.nextInt())

	abstract class ServerHandler<CH_DATA : BaseChannelData<CH_DATA, CLIENT_CH_DATA>, CLIENT_CH_DATA : ClientBaseChannelData<CLIENT_CH_DATA, CH_DATA>, PKT : BaseChannelListPacket<CH_DATA, CLIENT_CH_DATA>>(val persistentData: BasePersistentData<CH_DATA, *>) : IMessageHandler<PKT, PKT> {
		override fun onMessage(message: PKT, ctx: MessageContext): PKT? {
			val uuid = ctx.serverHandler.player.uniqueID

			persistentData.data.mapNotNullTo(message.channelData) { (id, channel) ->
				if(channel.deleted || channel.ownerUUID != uuid)
					return@mapNotNullTo null

				return@mapNotNullTo channel.toClientChannelData(id)
			}

			return message
		}
	}

	abstract class ClientHandler<CH_DATA : BaseChannelData<CH_DATA, CLIENT_CH_DATA>, CLIENT_CH_DATA : ClientBaseChannelData<CLIENT_CH_DATA, CH_DATA>, PKT : BaseChannelListPacket<CH_DATA, CLIENT_CH_DATA>>(val handlers: Int2ObjectArrayMap<(response: PKT) -> Unit>) : IMessageHandler<PKT, PKT> {
		override fun onMessage(message: PKT, ctx: MessageContext): PKT? {
			handlers.remove(message.id)?.invoke(message)
			return null
		}
	}
}
