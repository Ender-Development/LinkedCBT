package io.enderdev.linkedcbt.data

import io.enderdev.linkedcbt.Tags
import io.enderdev.linkedcbt.data.batteries.client.ClientBatteryChannelData
import io.enderdev.linkedcbt.data.tanks.client.ClientTankChannelData
import io.enderdev.linkedcbt.util.extensions.guiTranslate
import net.minecraft.util.ResourceLocation

internal object Constants {
	// special channel ids
	const val NO_CHANNEL = -1
	const val CREATE_NEW_CHANNEL = -101

	const val CHANNEL_NAME_LENGTH_LIMIT = 20

	const val LINKED_BT_GUI_PATH = "textures/gui/container/linked_bt_gui.png"
	val LINKED_BT_GUI = ResourceLocation(Tags.MOD_ID, LINKED_BT_GUI_PATH)

	// client-side only, used in [handleUpdateTag] to try to avoid creating useless class instances
	val NO_LINKED_POSITIONS = HashSet<DimBlockPos>(0)

	// special channel instances that have the id of [CREATE_NEW_CHANNEL]
	val CLIENT_TANK_CHANNEL_CREATE_NEW = ClientTankChannelData(CREATE_NEW_CHANNEL, "create_new".guiTranslate(), null, 0, 0)
	val CLIENT_BATTERY_CHANNEL_CREATE_NEW = ClientBatteryChannelData(CREATE_NEW_CHANNEL, "create_new".guiTranslate(), 0, 0)
}
