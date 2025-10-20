package io.enderdev.linkedcbt.network

import io.enderdev.linkedcbt.data.tanks.client.ClientTankChannelData
import io.enderdev.linkedcbt.data.tanks.LTPersistentData
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

class TankChannelListPacket : IMessage {
	var id: Int
	val channelData = mutableListOf<ClientTankChannelData>()

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
			channelData.add(ClientTankChannelData(buf.readInt(), buf.readString(), FluidRegistry.getFluid(buf.readString()), buf.readInt(), buf.readInt()))
		}
	}

	constructor(id: Int) {
		this.id = id
	}

	constructor() : this(Random.nextInt())

	class ServerHandler() : IMessageHandler<TankChannelListPacket, TankChannelListPacket> {
		override fun onMessage(message: TankChannelListPacket, ctx: MessageContext): TankChannelListPacket? {
			val uuid = ctx.serverHandler.player.uniqueID

			LTPersistentData.data.mapNotNullTo(message.channelData) { (id, channel) ->
				if(channel.deleted || channel.ownerUUID != uuid)
					return@mapNotNullTo null

				return@mapNotNullTo ClientTankChannelData(id, channel.name, channel.fluid, channel.fluidAmount, channel.fluidCapacity)
			}

			return message
		}
	}

	class ClientHandler() : IMessageHandler<TankChannelListPacket, TankChannelListPacket> {
		override fun onMessage(message: TankChannelListPacket, ctx: MessageContext): TankChannelListPacket? {
			handlers.remove(message.id)?.invoke(message)
			return null
		}
	}

	companion object {
		/**
		 * Client-side only
		 */
		val handlers: Int2ObjectMap<(response: TankChannelListPacket) -> Unit> = Int2ObjectArrayMap(2)
	}
}
