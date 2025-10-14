package io.enderdev.linkedtanks.integration

import io.enderdev.linkedtanks.Tags
import io.enderdev.linkedtanks.data.Constants
import io.enderdev.linkedtanks.tiles.TileLinkedTank
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
		val tile = world.getTileEntity(data.pos) as? TileLinkedTank ?: return

		info.text(when(tile.channelId) {
			Constants.NO_CHANNEL -> "Unlinked"
			else -> "Channel ${tile.channelData?.displayName(tile.channelId)}"
		})
	}
}
