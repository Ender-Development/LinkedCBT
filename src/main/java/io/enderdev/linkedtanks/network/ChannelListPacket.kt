package io.enderdev.linkedtanks.network

import io.enderdev.linkedtanks.client.ClientChannelData
import io.enderdev.linkedtanks.data.LTPersistentData
import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import org.ender_development.catalyx.utils.extensions.readString
import org.ender_development.catalyx.utils.extensions.writeString
import kotlin.random.Random

class ChannelListPacket : IMessage {
	var id: Int
	val channelData = mutableListOf<ClientChannelData>()

	override fun toBytes(buf: ByteBuf) {
		buf.writeInt(id)
		buf.writeInt(channelData.size)
		channelData.forEach {
			buf.writeInt(it.id)
			buf.writeString(it.name)
			buf.writeString(FluidRegistry.getFluidName(it.fluid) ?: "")
			buf.writeInt(it.fluidAmount)
			buf.writeInt(it.fluidCapacity)
		}
	}

	override fun fromBytes(buf: ByteBuf) {
		id = buf.readInt()
		repeat(buf.readInt()) {
			channelData.add(ClientChannelData(buf.readInt(), buf.readString(), FluidRegistry.getFluid(buf.readString()), buf.readInt(), buf.readInt()))
		}
	}

	constructor(id: Int) {
		this.id = id
	}

	constructor() : this(Random.nextInt())

	class ServerHandler() : IMessageHandler<ChannelListPacket, ChannelListPacket> {
		override fun onMessage(message: ChannelListPacket, ctx: MessageContext): ChannelListPacket? {
			val uuid = ctx.serverHandler.player.uniqueID

			LTPersistentData.data.mapNotNullTo(message.channelData) { (id, channel) ->
				if(channel.deleted || channel.ownerUUID != uuid)
					return@mapNotNullTo null

				return@mapNotNullTo ClientChannelData(id, channel.name, channel.fluid, channel.fluidAmount, channel.fluidCapacity)
			}

			return message
		}
	}

	class ClientHandler() : IMessageHandler<ChannelListPacket, ChannelListPacket> {
		override fun onMessage(message: ChannelListPacket, ctx: MessageContext): ChannelListPacket? {
			handlers.remove(message.id)?.invoke(message)
			return null
		}
	}

	companion object {
		/**
		 * Client-side only
		 */
		val handlers: Int2ObjectMap<(response: ChannelListPacket) -> Unit> = Int2ObjectArrayMap(2)
	}
}
