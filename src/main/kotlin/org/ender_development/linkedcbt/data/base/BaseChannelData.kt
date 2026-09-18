package org.ender_development.linkedcbt.data.base

import org.ender_development.linkedcbt.data.DimBlockPos
import org.ender_development.linkedcbt.data.base.client.ClientBaseChannelData
import java.util.*

abstract class BaseChannelData<CH_DATA : BaseChannelData<CH_DATA, CLIENT_CH_DATA>, CLIENT_CH_DATA : ClientBaseChannelData<CLIENT_CH_DATA, CH_DATA>> {
	/** UUID of the channel's owner */
	abstract var ownerUUID: UUID
	/** Username of the channel's owner, used in GUIs for information display */
	abstract var ownerUsername: String
	/** Name of the channel */
	abstract var name: String
	/** List of associated linked C/B/Ts */
	abstract val linkedPositions: HashSet<DimBlockPos>
	/** Unix millis timestamp of creation */
	abstract val creationTime: Long

	fun displayName() =
		name

	fun canBeEditedBy(uuid: UUID) =
		ownerUUID == uuid

	abstract fun toClientChannelData(id: UUID): CLIENT_CH_DATA
}
