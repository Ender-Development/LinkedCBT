package io.enderdev.linkedtanks.client.gui

import io.enderdev.linkedtanks.Tags
import io.enderdev.linkedtanks.client.container.ContainerLinkedTank
import io.enderdev.linkedtanks.tiles.TileLinkedTank
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.util.ResourceLocation
import org.ender_development.catalyx.client.gui.BaseGuiTyped

class GuiLinkedTank(playerInv: InventoryPlayer, tile: TileLinkedTank) : BaseGuiTyped<TileLinkedTank>(ContainerLinkedTank(playerInv, tile), tile) {
	override val textureLocation = ResourceLocation(Tags.MOD_ID, "textures/gui/container/linked_tank_gui.png")

	init {
		//this.displayData.add(CapabilityEnergyDisplayWrapper(8, 21, 16, 70, tile::energyStorage))
		//this.displayData.add(CapabilityFluidDisplayWrapper(44, 21, 16, 70, tile::inputTank))
	}

	override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY)
	}
}
