package io.enderdev.linkedcbt.data.base

import io.enderdev.linkedcbt.data.DimBlockPos
import io.enderdev.linkedcbt.data.base.client.ClientBaseChannelData
import java.util.*

abstract class BaseChannelData<CH_DATA : BaseChannelData<CH_DATA, CLIENT_CH_DATA>, CLIENT_CH_DATA : ClientBaseChannelData<CLIENT_CH_DATA, CH_DATA>> {
	abstract var deleted: Boolean
	abstract var ownerUUID: UUID
	abstract var ownerUsername: String
	abstract var name: String
	abstract val linkedPositions: HashSet<DimBlockPos>

	fun displayName(channelId: Int) =
		"#$channelId $name"

	fun canBeEditedBy(uuid: UUID) =
		ownerUUID == uuid

	abstract fun toClientChannelData(id: Int): CLIENT_CH_DATA
}
