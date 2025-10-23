package io.enderdev.linkedcbt.network

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.chests.ChestChannelData
import io.enderdev.linkedcbt.data.chests.LCPersistentData
import io.enderdev.linkedcbt.data.chests.client.ClientChestChannelData
import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import net.minecraft.item.ItemStack
import org.ender_development.catalyx.utils.extensions.readItemStack
import org.ender_development.catalyx.utils.extensions.readString
import org.ender_development.catalyx.utils.extensions.writeItemStack
import org.ender_development.catalyx.utils.extensions.writeString
import kotlin.random.Random

class ChestChannelListPacket : BaseChannelListPacket<ChestChannelData, ClientChestChannelData> {
	override fun writeChannel(buf: ByteBuf, channel: ClientChestChannelData) {
		buf.writeInt(channel.id)
		buf.writeString(channel.name)
		// TODO is this needed? Client stuff is only used for fake channel data temporarily and for CHANNEL_LINK
		// Fake channel data is very short-lived, so this might be useless
		// also applies to CCD for Tanks and Batteries
		channel.items.forEachIndexed { idx, stack ->
			if(!stack.isEmpty) {
				buf.writeInt(idx)
				buf.writeItemStack(stack)
			}
		}
		buf.writeInt(-1)
	}

	override fun readChannel(buf: ByteBuf) =
		ClientChestChannelData(buf.readInt(), buf.readString(), Array(Constants.LINKED_CHEST_INVENTORY_SIZE) { ItemStack.EMPTY }.also {
			while(true) {
				val idx = buf.readInt()
				if(idx !in it.indices) // we use -1 to signify end of list, but might as well check everything
					return@also

				it[idx] = buf.readItemStack()
			}
		})

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
