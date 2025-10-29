package io.enderdev.linkedcbt.blocks

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.client.container.ContainerLinkedBattery
import io.enderdev.linkedcbt.client.gui.GuiLinkedBattery
import io.enderdev.linkedcbt.tiles.TileLinkedBattery
import net.minecraft.block.BlockHorizontal
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import org.ender_development.catalyx.blocks.BaseRotatableMachineBlock

class LinkedBatteryBlock : BaseRotatableMachineBlock(LinkedCBT, "linked_battery", LinkedCBT.guiHandler.registerId(TileLinkedBattery::class.java, ContainerLinkedBattery::class.java) { GuiLinkedBattery::class.java }) {
	companion object {
		val hasEnergy: PropertyBool = PropertyBool.create("has_energy")
	}

	init {
		defaultState = defaultState.withProperty(hasEnergy, false)
	}

	override fun createBlockState() =
		BlockStateContainer(this, BlockHorizontal.FACING, hasEnergy)

	override fun breakBlock(world: World, pos: BlockPos, state: IBlockState) {
		// super destroys the TE
		(world.getTileEntity(pos) as? TileLinkedBattery)?.notifyBreak()
		super.breakBlock(world, pos, state)
	}

	@Deprecated("Implementation is fine.")
	override fun getActualState(state: IBlockState, world: IBlockAccess, pos: BlockPos): IBlockState {
		val tile = world.getTileEntity(pos) as? TileLinkedBattery ?: return state
		return state.withProperty(hasEnergy, tile.channelData?.energyAmount?.compareTo(0) == 1)
	}
}
