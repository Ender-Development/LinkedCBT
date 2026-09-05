package org.ender_development.linkedcbt.data.base.client

import org.ender_development.linkedcbt.data.Constants
import org.ender_development.linkedcbt.data.base.BaseChannelData

abstract class ClientBaseChannelData<CLIENT_CH_DATA : ClientBaseChannelData<CLIENT_CH_DATA, CH_DATA>, CH_DATA : BaseChannelData<CH_DATA, CLIENT_CH_DATA>> {
	abstract val id: Int
	abstract val name: String

	val displayName: String
		get() = if(id == Constants.CREATE_NEW_CHANNEL) "+ $name" else "#$id $name"

	abstract fun toFakeChannelData(): CH_DATA
}
