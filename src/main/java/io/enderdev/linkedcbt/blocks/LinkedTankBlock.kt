package io.enderdev.linkedcbt.blocks

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.client.container.ContainerLinkedTank
import io.enderdev.linkedcbt.client.gui.GuiLinkedTank
import io.enderdev.linkedcbt.tiles.TileLinkedTank
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.ender_development.catalyx.blocks.BaseMachineBlock

class LinkedTankBlock : BaseMachineBlock(LinkedCBT, "linked_tank", LinkedCBT.guiHandler.registerId(TileLinkedTank::class.java, ContainerLinkedTank::class.java) { GuiLinkedTank::class.java }) {
	override fun breakBlock(world: World, pos: BlockPos, state: IBlockState) {
		// super destroys the TE
		(world.getTileEntity(pos) as? TileLinkedTank)?.notifyBreak()
		super.breakBlock(world, pos, state)
	}
}
