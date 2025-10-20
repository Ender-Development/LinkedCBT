package io.enderdev.linkedcbt.client

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.network.ChannelListPacket
import io.enderdev.linkedcbt.network.PacketHandler

object ClientChannelListManager {
	private var lastQueried = 0L
	private var cachedData = emptyList<ClientChannelData>()

	val channels: List<ClientChannelData>
		get() {
			// only check every 1s
			if(System.currentTimeMillis() - lastQueried >= 1000L) {
				lastQueried = System.currentTimeMillis()
				fetchNewData()
			}

			return cachedData
		}

	private fun fetchNewData() {
		PacketHandler.channel.sendToServer(ChannelListPacket().also {
			ChannelListPacket.handlers.put(it.id) {
				it.channelData.sortBy { it.id }
				it.channelData.add(Constants.CLIENT_CHANNEL_CREATE_NEW)
				cachedData = it.channelData
			}
		})
	}
}
