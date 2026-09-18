package org.ender_development.linkedcbt.data

import net.minecraft.util.ResourceLocation
import org.ender_development.linkedcbt.Reference
import org.ender_development.linkedcbt.data.batteries.client.ClientBatteryChannelData
import org.ender_development.linkedcbt.data.chests.client.ClientChestChannelData
import org.ender_development.linkedcbt.data.tanks.client.ClientTankChannelData
import org.ender_development.linkedcbt.util.extensions.guiTranslate
import java.util.UUID

internal object Constants {
	// special channel ids
	val NO_CHANNEL = UUID(-1L, 0)
	val CREATE_NEW_CHANNEL = UUID(-2L, 0)

	const val CHANNEL_NAME_LENGTH_LIMIT = 20

	const val LINKED_CBT_GUI_PATH = "textures/gui/container/linked_cbt_gui.png"
	val LINKED_CBT_GUI = ResourceLocation(Reference.MODID, LINKED_CBT_GUI_PATH)

	const val LINKED_CHEST_INVENTORY_SIZE = 9 * 3

	// client-side only, used to try to avoid creating useless class instances
	val NO_LINKED_POSITIONS = HashSet<DimBlockPos>(0)

	// special channel instances that have the id of [CREATE_NEW_CHANNEL]
	val CLIENT_TANK_CHANNEL_CREATE_NEW = ClientTankChannelData(CREATE_NEW_CHANNEL, "create_new".guiTranslate(), 0)
	val CLIENT_BATTERY_CHANNEL_CREATE_NEW = ClientBatteryChannelData(CREATE_NEW_CHANNEL, "create_new".guiTranslate(), 0)
	val CLIENT_CHEST_CHANNEL_CREATE_NEW = ClientChestChannelData(CREATE_NEW_CHANNEL, "create_new".guiTranslate(), 0)

	const val DEBUG = true
}
