package io.enderdev.linkedcbt.client.gui

import io.enderdev.linkedcbt.client.container.ContainerLinkedBattery
import io.enderdev.linkedcbt.data.batteries.BatteryChannelData
import io.enderdev.linkedcbt.data.batteries.client.ClientBatteryChannelData
import io.enderdev.linkedcbt.data.batteries.client.ClientBatteryChannelListManager
import io.enderdev.linkedcbt.tiles.TileLinkedBattery
import net.minecraft.inventory.IInventory
import org.ender_development.catalyx.client.gui.wrappers.CapabilityEnergyDisplayWrapper

class GuiLinkedBattery(playerInv: IInventory, tile: TileLinkedBattery) : BaseLinkedGui<BatteryChannelData, ClientBatteryChannelData, TileLinkedBattery>(ContainerLinkedBattery(playerInv, tile), tile, ClientBatteryChannelListManager) {
	override val displayWrapper = CapabilityEnergyDisplayWrapper(BAR_X, BAR_Y, BAR_W, BAR_H, tile::energyStorage)
}
