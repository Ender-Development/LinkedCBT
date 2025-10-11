package io.enderdev.linkedtanks.client.gui

import io.enderdev.linkedtanks.Tags
import io.enderdev.linkedtanks.client.container.ContainerLinkedTank
import io.enderdev.linkedtanks.tiles.TileLinkedTank
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.util.ResourceLocation
import org.ender_development.catalyx.client.button.AbstractButtonWrapper
import org.ender_development.catalyx.client.gui.BaseGuiTyped
import org.ender_development.catalyx.client.gui.wrappers.CapabilityFluidDisplayWrapper

class GuiLinkedTank(playerInv: InventoryPlayer, tile: TileLinkedTank) : BaseGuiTyped<TileLinkedTank>(ContainerLinkedTank(playerInv, tile), tile) {
	override val textureLocation = ResourceLocation(Tags.MOD_ID, "textures/gui/container/linked_tank_gui.png")

	init {
		displayData.add(CapabilityFluidDisplayWrapper(44, 21, 16, 70, tile::fluidHandler))
	}

	lateinit var linkButton: TileLinkedTank.LinkButtonWrapper

	override fun initGui() {
		super.initGui()
		// remove the default buttons cause meh
		buttonList.removeIf { it is AbstractButtonWrapper.WrappedGuiButton }

		linkButton = TileLinkedTank.LinkButtonWrapper(guiLeft + 10, guiTop + 10)
		buttonList.add(linkButton.button)
	}

	// TODO: link GUI ;p
	// - current link state (fluid, amount, id, ownername)
	// - menu to link to a new group
	// - renaming current group
}
