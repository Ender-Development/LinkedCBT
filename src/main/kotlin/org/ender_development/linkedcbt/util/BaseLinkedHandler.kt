package org.ender_development.linkedcbt.util

import org.ender_development.linkedcbt.data.base.BaseChannelData

abstract class BaseLinkedHandler<TYPE, CH_DATA : BaseChannelData<CH_DATA, *>> {
	abstract var channelData: CH_DATA?
}
