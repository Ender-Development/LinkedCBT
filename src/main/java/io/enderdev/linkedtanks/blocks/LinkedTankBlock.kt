package io.enderdev.linkedtanks.blocks

import io.enderdev.linkedtanks.LinkedTanks
import io.enderdev.linkedtanks.client.container.ContainerLinkedTank
import io.enderdev.linkedtanks.client.gui.GuiLinkedTank
import io.enderdev.linkedtanks.tiles.TileLinkedTank
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.ender_development.catalyx.blocks.BaseMachineBlock

class LinkedTankBlock : BaseMachineBlock(LinkedTanks.modSettings, "linked_tank", TileLinkedTank::class.java, LinkedTanks.guiHandler.registerId(TileLinkedTank::class.java, ContainerLinkedTank::class.java) { GuiLinkedTank::class.java }) {
	override fun breakBlock(world: World, pos: BlockPos, state: IBlockState) {
		// super destroys the TE
		(world.getTileEntity(pos) as? TileLinkedTank)?.notifyBreak()
		super.breakBlock(world, pos, state)
	}
}
