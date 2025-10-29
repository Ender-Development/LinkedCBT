package io.enderdev.linkedcbt.blocks

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.client.container.ContainerLinkedTank
import io.enderdev.linkedcbt.client.gui.GuiLinkedTank
import io.enderdev.linkedcbt.tiles.TileLinkedTank
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import org.ender_development.catalyx.blocks.BaseRotatableMachineBlock

class LinkedTankBlock : BaseRotatableMachineBlock(LinkedCBT, "linked_tank", LinkedCBT.guiHandler.registerId(TileLinkedTank::class.java, ContainerLinkedTank::class.java) { GuiLinkedTank::class.java }) {
	override fun breakBlock(world: World, pos: BlockPos, state: IBlockState) {
		// super destroys the TE
		(world.getTileEntity(pos) as? TileLinkedTank)?.notifyBreak()
		super.breakBlock(world, pos, state)
	}

	@Deprecated("")
	override fun shouldSideBeRendered(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing): Boolean {
		val other = blockAccess.getBlockState(pos.offset(side)).block
		return other !== ModBlocks.linkedBattery && other !== ModBlocks.linkedTank && other !== ModBlocks.linkedChest
	}

	// TODO: Once the TESR stuff in catalyx is ready, use it to draw the fluid inside the tank on its block model
	// We already do something similar in Alchemistry Redox with the Evaporator, so it can possibly be reused here
	// ^ this doesn't need to be a TESR, it can be a simple BlockColorHandler thingy
}
