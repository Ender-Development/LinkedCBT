package io.enderdev.linkedtanks.tiles

import io.enderdev.linkedtanks.LinkedTanks
import net.minecraft.util.ITickable
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate
import org.ender_development.catalyx.client.button.AbstractButtonWrapper
import org.ender_development.catalyx.client.button.PauseButtonWrapper
import org.ender_development.catalyx.client.button.RedstoneButtonWrapper
import org.ender_development.catalyx.client.gui.BaseGui
import org.ender_development.catalyx.client.gui.BaseGuiTyped
import org.ender_development.catalyx.tiles.BaseTile
import org.ender_development.catalyx.tiles.helper.IButtonTile
import org.ender_development.catalyx.tiles.helper.IFluidTile
import org.ender_development.catalyx.tiles.helper.IGuiTile

class TileLinkedTank : BaseTile(LinkedTanks.modSettings), IFluidTile, ITickable, IGuiTile, IButtonTile, BaseGuiTyped.IDefaultButtonVariables {
	override var isPaused = false
	override var needsRedstonePower = false

	override val fluidTanks: FluidHandlerConcatenate
		get() = TODO("Not yet implemented")

	override fun update() {
		TODO("Not yet implemented")
	}

	override fun handleButtonPress(button: AbstractButtonWrapper) {
		when(button) {
			is PauseButtonWrapper -> isPaused = !isPaused
			is RedstoneButtonWrapper -> needsRedstonePower = !needsRedstonePower
		}
	}
}
