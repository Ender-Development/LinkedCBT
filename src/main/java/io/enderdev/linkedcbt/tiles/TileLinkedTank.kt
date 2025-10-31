package io.enderdev.linkedcbt.tiles

import io.enderdev.linkedcbt.blocks.LinkedTankBlock
import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.tanks.LTPersistentData
import io.enderdev.linkedcbt.data.tanks.TankChannelData
import io.enderdev.linkedcbt.tiles.util.FluidSideConfiguration
import io.enderdev.linkedcbt.util.LinkedFluidHandler
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fluids.capability.IFluidHandler
import org.ender_development.catalyx.tiles.helper.IFluidTile
import java.util.*

class TileLinkedTank : BaseLinkedTile<TileLinkedTank, TankChannelData, IFluidHandler, LinkedFluidHandler>(LTPersistentData, FLUID_CAP), IFluidTile {
	override val sideConfiguration = FluidSideConfiguration(this)
	override val linkedHandler = LinkedFluidHandler(channelData)
	override val fluidHandler = linkedHandler

	override fun writeClientChannelData(channelData: TankChannelData, tag: NBTTagCompound) {
		if(channelData.fluid != null)
			tag.setString("FluidName", FluidRegistry.getFluidName(channelData.fluid))
		tag.setInteger("FluidAmount", channelData.fluidAmount)
		tag.setInteger("FluidCapacity", channelData.fluidCapacity)
	}

	override fun readClientChannelData(tag: NBTTagCompound, name: String, ownerUsername: String, ownerUUID: UUID): TankChannelData {
		val fluid = FluidRegistry.getFluid(tag.getString("FluidName"))
		val fluidAmount = tag.getInteger("FluidAmount")
		return TankChannelData(false, ownerUUID, ownerUsername, name, fluid, fluidAmount, Constants.NO_LINKED_POSITIONS).apply {
			fluidCapacityOverride = tag.getInteger("FluidCapacity")
		}
	}

	override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
		if(channelId == Constants.NO_CHANNEL || !channelData!!.canBeEditedBy(player.uniqueID))
			return false

		val heldItem = player.getHeldItem(hand)
		if(heldItem.hasCapability(ITEM_FLUID_CAP, facing) || heldItem.hasCapability(FLUID_CAP, facing))
			FluidUtil.interactWithFluidHandler(player, hand, world, pos, facing).also { markDirtyGUI() }

		return false
	}

	val currentlyHasFluid
		inline get() = channelData?.fluid != null

	var hasFluidPreviously = false
	// change the behaviour of super@BaseLinkedTile without overriding everything because this is easier
	override fun markDirtyGUI() {
		val currentlyHasFluid = currentlyHasFluid
		if(world != null && currentlyHasFluid != hasFluidPreviously) {
			// similar to [markDirtyClient]
			world.setBlockState(pos, world.getBlockState(pos).withProperty(LinkedTankBlock.forceUpdate, currentlyHasFluid), 2)
			markDirty()
			hasFluidPreviously = currentlyHasFluid
		} else
			super.markDirtyGUI()
	}
}
