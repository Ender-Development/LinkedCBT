package io.enderdev.linkedcbt.util

import io.enderdev.linkedcbt.data.base.BaseChannelData

abstract class BaseLinkedHandler<TYPE, CH_DATA : BaseChannelData<CH_DATA, *>> {
	abstract var channelData: CH_DATA?
}
