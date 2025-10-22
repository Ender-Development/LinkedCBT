package io.enderdev.linkedcbt.data.tanks.client

import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.base.client.BaseClientChannelListManager
import io.enderdev.linkedcbt.data.tanks.TankChannelData
import io.enderdev.linkedcbt.network.TankChannelListPacket

object ClientTankChannelListManager : BaseClientChannelListManager<TankChannelData, ClientTankChannelData, TankChannelListPacket>(TankChannelListPacket::class.java, TankChannelListPacket.handlers, Constants.CLIENT_TANK_CHANNEL_CREATE_NEW)
