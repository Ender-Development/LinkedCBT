package io.enderdev.linkedcbt.blocks

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.client.container.ContainerLinkedChest
import io.enderdev.linkedcbt.client.gui.GuiLinkedChest
import io.enderdev.linkedcbt.tiles.TileLinkedChest
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import org.ender_development.catalyx.blocks.BaseRotatableMachineBlock

class LinkedChestBlock : BaseRotatableMachineBlock(LinkedCBT, "linked_chest", LinkedCBT.guiHandler.registerId(TileLinkedChest::class.java, ContainerLinkedChest::class.java) { GuiLinkedChest::class.java }) {
	override fun breakBlock(world: World, pos: BlockPos, state: IBlockState) {
		// super destroys the TE
		(world.getTileEntity(pos) as? TileLinkedChest)?.notifyBreak()
		super.breakBlock(world, pos, state)
	}

	@Deprecated("")
	override fun shouldSideBeRendered(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing): Boolean {
		val other = blockAccess.getBlockState(pos.offset(side)).block
		return other !== ModBlocks.linkedBattery && other !== ModBlocks.linkedTank && other !== ModBlocks.linkedChest
	}
}
