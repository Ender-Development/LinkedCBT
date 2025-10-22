package io.enderdev.linkedcbt.data.batteries.client

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.base.client.BaseClientChannelListManager
import io.enderdev.linkedcbt.data.batteries.BatteryChannelData
import io.enderdev.linkedcbt.network.BatteryChannelListPacket

object ClientBatteryChannelListManager : BaseClientChannelListManager<BatteryChannelData, ClientBatteryChannelData, BatteryChannelListPacket>(BatteryChannelListPacket::class.java, BatteryChannelListPacket.handlers, Constants.CLIENT_BATTERY_CHANNEL_CREATE_NEW)
