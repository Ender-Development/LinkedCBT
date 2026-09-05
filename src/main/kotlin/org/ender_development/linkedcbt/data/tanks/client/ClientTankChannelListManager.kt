package org.ender_development.linkedcbt.data.tanks.client

import org.ender_development.linkedcbt.data.Constants
import org.ender_development.linkedcbt.data.base.client.BaseClientChannelListManager
import org.ender_development.linkedcbt.data.tanks.TankChannelData
import org.ender_development.linkedcbt.network.TankChannelListPacket

object ClientTankChannelListManager : BaseClientChannelListManager<TankChannelData, ClientTankChannelData, TankChannelListPacket>(TankChannelListPacket::class.java, TankChannelListPacket.handlers, Constants.CLIENT_TANK_CHANNEL_CREATE_NEW)
