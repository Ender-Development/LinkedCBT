package io.enderdev.linkedcbt.integration

import io.enderdev.linkedcbt.Tags
import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.tiles.BaseLinkedTile
import io.enderdev.linkedcbt.tiles.TileLinkedTank
import mcjty.theoneprobe.api.IProbeHitData
import mcjty.theoneprobe.api.IProbeInfo
import mcjty.theoneprobe.api.IProbeInfoProvider
import mcjty.theoneprobe.api.ProbeMode
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World

object LTTOPIntegration : IProbeInfoProvider {
	override fun getID() =
		Tags.MOD_ID

	override fun addProbeInfo(mode: ProbeMode, info: IProbeInfo, player: EntityPlayer, world: World, state: IBlockState, data: IProbeHitData) {
		val tile = world.getTileEntity(data.pos) as? BaseLinkedTile<*, *, *, *> ?: return

		info.text(when(tile.channelId) {
			Constants.NO_CHANNEL -> "Unlinked"
			else -> "Channel ${tile.channelData?.displayName(tile.channelId)}"
		})
	}
}
