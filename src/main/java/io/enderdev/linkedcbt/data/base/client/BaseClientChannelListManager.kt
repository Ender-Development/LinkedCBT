package io.enderdev.linkedcbt.data.base.client

import io.enderdev.linkedcbt.data.base.BaseChannelData
import io.enderdev.linkedcbt.network.BaseChannelListPacket
import io.enderdev.linkedcbt.network.PacketHandler
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap

abstract class BaseClientChannelListManager<CH_DATA : BaseChannelData<CH_DATA, CLIENT_CH_DATA>, CLIENT_CH_DATA : ClientBaseChannelData<CLIENT_CH_DATA, CH_DATA>, PKT : BaseChannelListPacket<CH_DATA, CLIENT_CH_DATA>>(val packet: Class<out PKT>, val handlers: Int2ObjectArrayMap<(PKT) -> Unit>, val createNewChannel: CLIENT_CH_DATA) {
	private var lastQueried = 0L
	private var cachedData = listOf(createNewChannel)

	val channels: List<CLIENT_CH_DATA>
		get() {
			// only check every 1s
			if(System.currentTimeMillis() - lastQueried >= 1000L) {
				lastQueried = System.currentTimeMillis()
				fetchNewData()
			}

			return cachedData
		}

	private fun fetchNewData() {
		PacketHandler.channel.sendToServer(packet.newInstance().also {
			handlers.put(it.id) {
				it.channelData.sortBy { it.id }
				it.channelData.add(createNewChannel)
				cachedData = it.channelData
			}
		})
	}
}
