package io.enderdev.linkedcbt.blocks

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.client.container.ContainerLinkedBattery
import io.enderdev.linkedcbt.client.gui.GuiLinkedBattery
import io.enderdev.linkedcbt.tiles.TileLinkedBattery
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.ender_development.catalyx.blocks.BaseMachineBlock

class LinkedBatteryBlock : BaseMachineBlock(LinkedCBT, "linked_battery", LinkedCBT.guiHandler.registerId(TileLinkedBattery::class.java, ContainerLinkedBattery::class.java) { GuiLinkedBattery::class.java }) {
	override fun breakBlock(world: World, pos: BlockPos, state: IBlockState) {
		// super destroys the TE
		(world.getTileEntity(pos) as? TileLinkedBattery)?.notifyBreak()
		super.breakBlock(world, pos, state)
	}
}
