package io.enderdev.linkedtanks.client

import io.enderdev.linkedtanks.network.ChannelListPacket
import io.enderdev.linkedtanks.network.PacketHandler
import io.enderdev.linkedtanks.tiles.TileLinkedTank

object ClientChannelListManager {
	var lastGot = 0L
	private var cachedData: List<ChannelListPacket.ClientChannelData> = emptyList()

	val channels: List<ChannelListPacket.ClientChannelData>
		get() {
			// only check every 1s
			if(System.currentTimeMillis() - lastGot >= 1000L) {
				lastGot = System.currentTimeMillis()
				fetch()
			}

			return cachedData
		}

	private fun fetch() {
		PacketHandler.channel.sendToServer(ChannelListPacket().also {
			ChannelListPacket.handlers.put(it.id) {
				it.channelData.sortBy { it.id }
				it.channelData.add(createNewChannel)
				cachedData = it.channelData
			}
		})
	}

	val createNewChannel = ChannelListPacket.ClientChannelData(TileLinkedTank.CREATE_NEW_CHANNEL, "Create new", null, 0, 0)
}
