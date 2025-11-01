package io.enderdev.linkedcbt.blocks

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.client.container.ContainerLinkedBattery
import io.enderdev.linkedcbt.client.gui.GuiLinkedBattery
import io.enderdev.linkedcbt.tiles.TileLinkedBattery
import net.minecraft.block.BlockHorizontal
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import org.ender_development.catalyx.blocks.BaseRotatableMachineBlock

class LinkedBatteryBlock : BaseLinkedBlock("battery", LinkedCBT.guiHandler.registerId(TileLinkedBattery::class.java, ContainerLinkedBattery::class.java) { GuiLinkedBattery::class.java }) {
	companion object {
		val hasEnergy: PropertyBool = PropertyBool.create("has_energy")
	}

	init {
		defaultState = defaultState.withProperty(hasEnergy, false)
	}

	override fun createBlockState() =
		BlockStateContainer(this, BlockHorizontal.FACING, hasEnergy)

	override fun getStateForPlacement(world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase, hand: EnumHand): IBlockState =
		super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(hasEnergy, false)

	@Deprecated("")
	override fun getStateFromMeta(meta: Int): IBlockState =
		@Suppress("DEPRECATION")
		super.getStateFromMeta(meta and 0b0011).withProperty(hasEnergy, meta and 0b0100 != 0)

	override fun getMetaFromState(state: IBlockState) =
		super.getMetaFromState(state) or (if(state.getValue(hasEnergy)) 0b0100 else 0)
}
