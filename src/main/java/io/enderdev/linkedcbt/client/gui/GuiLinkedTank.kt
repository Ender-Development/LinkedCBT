package io.enderdev.linkedcbt.client.gui

import io.enderdev.linkedcbt.client.container.ContainerLinkedTank
import io.enderdev.linkedcbt.data.tanks.TankChannelData
import io.enderdev.linkedcbt.data.tanks.client.ClientTankChannelData
import io.enderdev.linkedcbt.data.tanks.client.ClientTankChannelListManager
import io.enderdev.linkedcbt.tiles.TileLinkedTank
import net.minecraft.inventory.IInventory
import org.ender_development.catalyx.client.gui.wrappers.CapabilityFluidDisplayWrapper

class GuiLinkedTank(playerInv: IInventory, tile: TileLinkedTank) : BaseLinkedGui<TankChannelData, ClientTankChannelData, TileLinkedTank>(ContainerLinkedTank(playerInv, tile), tile, ClientTankChannelListManager) {
	override val displayWrapper = CapabilityFluidDisplayWrapper(BAR_X, BAR_Y, BAR_W, BAR_H, tile::fluidHandler)

	// TODO
	// - fluid whitelist selector?
}
