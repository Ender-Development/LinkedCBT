package io.enderdev.linkedtanks.items

import io.enderdev.linkedtanks.LinkedTanks
import io.enderdev.linkedtanks.tiles.TileLinkedTank
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentString
import net.minecraft.world.World
import org.ender_development.catalyx.items.BaseItem

class ItemTankConfigurator : BaseItem(LinkedTanks.modSettings, "tank_configurator") {
	override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {
		val te = world.getTileEntity(pos) as? TileLinkedTank ?: return EnumActionResult.PASS
		if(te.channelData != null && te.channelData?.canBeEditedBy(player.uniqueID) == false)
			return EnumActionResult.PASS

		val newSideConfiguration = te.fluidSideConfiguration[facing].next()
		te.fluidSideConfiguration[facing] = newSideConfiguration
		player.sendMessage(TextComponentString(newSideConfiguration.named.replaceFirstChar(Char::uppercaseChar)).setStyle(Style().setColor(newSideConfiguration.colour)))

		return EnumActionResult.SUCCESS
	}
}
