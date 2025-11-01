package io.enderdev.linkedcbt.blocks

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.tiles.BaseLinkedTile
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.ender_development.catalyx.blocks.BaseRotatableMachineBlock

abstract class BaseLinkedBlock(type: String, guiId: Int) : BaseRotatableMachineBlock(LinkedCBT, "linked_$type", guiId) {
	override fun breakBlock(world: World, pos: BlockPos, state: IBlockState) {
		// super destroys the TE
		(world.getTileEntity(pos) as? BaseLinkedTile<*, *, *, *>)?.notifyBreak()
		super.breakBlock(world, pos, state)
	}

	@Deprecated("")
	override fun shouldSideBeRendered(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing): Boolean {
		val other = blockAccess.getBlockState(pos.offset(side)).block
		return other !== ModBlocks.linkedBattery && other !== ModBlocks.linkedTank && other !== ModBlocks.linkedChest
	}

	@Deprecated(".")
	override fun isOpaqueCube(state: IBlockState) =
		false

	@SideOnly(Side.CLIENT)
	@Deprecated("")
	override fun getAmbientOcclusionLightValue(state: IBlockState) =
		.2f
}
