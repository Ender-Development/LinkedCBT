package org.ender_development.linkedcbt.data.base.client

import org.ender_development.linkedcbt.data.Constants
import org.ender_development.linkedcbt.data.base.BaseChannelData
import java.util.*

abstract class ClientBaseChannelData<CLIENT_CH_DATA : ClientBaseChannelData<CLIENT_CH_DATA, CH_DATA>, CH_DATA : BaseChannelData<CH_DATA, CLIENT_CH_DATA>> {
	abstract val id: UUID
	abstract val name: String
	abstract val creationTime: Long

	val displayName: String
		get() = if(id == Constants.CREATE_NEW_CHANNEL) "+ $name" else name

	abstract fun toFakeChannelData(): CH_DATA
}
