package io.enderdev.linkedcbt.data.chests.client

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.base.client.BaseClientChannelListManager
import io.enderdev.linkedcbt.data.chests.ChestChannelData
import io.enderdev.linkedcbt.network.ChestChannelListPacket

object ClientChestChannelListManager : BaseClientChannelListManager<ChestChannelData, ClientChestChannelData, ChestChannelListPacket>(ChestChannelListPacket::class.java, ChestChannelListPacket.handlers, Constants.CLIENT_CHEST_CHANNEL_CREATE_NEW)
