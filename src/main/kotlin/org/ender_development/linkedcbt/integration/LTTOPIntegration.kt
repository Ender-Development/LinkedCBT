package org.ender_development.linkedcbt.integration

import mcjty.theoneprobe.api.IProbeHitData
import mcjty.theoneprobe.api.IProbeInfo
import mcjty.theoneprobe.api.IProbeInfoProvider
import mcjty.theoneprobe.api.ProbeMode
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import org.ender_development.linkedcbt.Reference
import org.ender_development.linkedcbt.data.Constants
import org.ender_development.linkedcbt.tiles.BaseLinkedTile

object LTTOPIntegration : IProbeInfoProvider {
	override fun getID() =
        Reference.MODID

	override fun addProbeInfo(mode: ProbeMode, info: IProbeInfo, player: EntityPlayer, world: World, state: IBlockState, data: IProbeHitData) {
		val tile = world.getTileEntity(data.pos) as? BaseLinkedTile<*, *, *, *> ?: return

		info.text(when(tile.channelId) {
			Constants.NO_CHANNEL -> "Unlinked"
			else -> "Channel ${tile.channelData?.displayName(tile.channelId)}"
		})
	}
}
