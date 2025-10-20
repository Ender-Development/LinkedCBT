package io.enderdev.linkedcbt.data.tanks.client

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.network.TankChannelListPacket
import io.enderdev.linkedcbt.network.PacketHandler

object ClientTankChannelListManager {
	private var lastQueried = 0L
	private var cachedData = emptyList<ClientTankChannelData>()

	val channels: List<ClientTankChannelData>
		get() {
			// only check every 1s
			if(System.currentTimeMillis() - lastQueried >= 1000L) {
				lastQueried = System.currentTimeMillis()
				fetchNewData()
			}

			return cachedData
		}

	private fun fetchNewData() {
		PacketHandler.channel.sendToServer(TankChannelListPacket().also {
			TankChannelListPacket.handlers.put(it.id) {
				it.channelData.sortBy { it.id }
				it.channelData.add(Constants.CLIENT_CHANNEL_CREATE_NEW)
				cachedData = it.channelData
			}
		})
	}
}
