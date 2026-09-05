package org.ender_development.linkedcbt.client.gui

import org.ender_development.linkedcbt.client.container.ContainerLinkedTank
import org.ender_development.linkedcbt.data.tanks.TankChannelData
import org.ender_development.linkedcbt.data.tanks.client.ClientTankChannelData
import org.ender_development.linkedcbt.data.tanks.client.ClientTankChannelListManager
import org.ender_development.linkedcbt.tiles.TileLinkedTank
import net.minecraft.inventory.IInventory
import org.ender_development.catalyx.core.client.gui.wrappers.CapabilityFluidDisplayWrapper

class GuiLinkedTank(playerInv: IInventory, tile: TileLinkedTank) : BaseLinkedGui<TankChannelData, ClientTankChannelData, TileLinkedTank>(ContainerLinkedTank(playerInv, tile), tile, ClientTankChannelListManager) {
	override val displayWrapper = CapabilityFluidDisplayWrapper(BAR_X, BAR_Y, BAR_W, BAR_H, tile::fluidHandler)

	// TODO - fluid whitelist selector?
}
