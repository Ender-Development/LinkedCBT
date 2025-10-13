package io.enderdev.linkedtanks.items

import io.enderdev.linkedtanks.LinkedTanks
import io.enderdev.linkedtanks.tiles.TileLinkedTank
import io.enderdev.linkedtanks.util.extensions.reply
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.ender_development.catalyx.items.BaseItem

class ItemTankConfigurator : BaseItem(LinkedTanks.modSettings, "tank_configurator") {
	// TODO texture

	override fun onItemUseFirst(player: EntityPlayer, world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, hand: EnumHand): EnumActionResult {
		if(world.isRemote || hand != EnumHand.MAIN_HAND)
			return EnumActionResult.PASS

		val te = world.getTileEntity(pos) as? TileLinkedTank ?: return EnumActionResult.PASS
		if(te.channelData != null && te.channelData?.canBeEditedBy(player.uniqueID) == false)
			return EnumActionResult.PASS

		val newSideConfiguration = te.fluidSideConfiguration[facing].let {
			if(player.isSneaking)
				it.previous()
			else
				it.next()
		}
		te.fluidSideConfiguration[facing] = newSideConfiguration
		player.reply(newSideConfiguration.describe(facing), newSideConfiguration.colour)

		return EnumActionResult.SUCCESS
	}
}
