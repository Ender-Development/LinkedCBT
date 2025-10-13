package io.enderdev.linkedtanks.data

import io.enderdev.linkedtanks.client.ClientChannelData

internal object Constants {
	// special channel ids
	const val NO_CHANNEL = -1
	const val CREATE_NEW_CHANNEL = -101

	const val CHANNEL_NAME_LENGTH_LIMIT = 20

	// client-side only, used in [handleUpdateTag] to try to avoid creating useless class instances
	val NO_LINKED_POSITIONS = HashSet<DimBlockPos>(0)

	// special [ClientChannelData] instance that has the id of [CREATE_NEW_CHANNEL]
	val CLIENT_CHANNEL_CREATE_NEW = ClientChannelData(CREATE_NEW_CHANNEL, "Create new", null, 0, 0)
}
