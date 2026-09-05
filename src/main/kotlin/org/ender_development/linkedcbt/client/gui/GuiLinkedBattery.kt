package org.ender_development.linkedcbt.client.gui

import org.ender_development.linkedcbt.client.container.ContainerLinkedBattery
import org.ender_development.linkedcbt.data.batteries.BatteryChannelData
import org.ender_development.linkedcbt.data.batteries.client.ClientBatteryChannelData
import org.ender_development.linkedcbt.data.batteries.client.ClientBatteryChannelListManager
import org.ender_development.linkedcbt.tiles.TileLinkedBattery
import net.minecraft.inventory.IInventory
import org.ender_development.catalyx.core.client.gui.wrappers.CapabilityEnergyDisplayWrapper

class GuiLinkedBattery(playerInv: IInventory, tile: TileLinkedBattery) : BaseLinkedGui<BatteryChannelData, ClientBatteryChannelData, TileLinkedBattery>(ContainerLinkedBattery(playerInv, tile), tile, ClientBatteryChannelListManager) {
	override val displayWrapper = CapabilityEnergyDisplayWrapper(BAR_X, BAR_Y, BAR_W, BAR_H, tile::energyStorage)
}
