package io.enderdev.linkedcbt.data.tanks

import io.enderdev.linkedcbt.data.DimBlockPos
import io.enderdev.linkedcbt.data.base.BasePersistentData
import io.enderdev.linkedcbt.tiles.TileLinkedTank
import io.enderdev.linkedcbt.util.extensions.dim
import io.enderdev.linkedcbt.util.extensions.dimId
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids.FluidRegistry

object LTPersistentData : BasePersistentData<TankChannelData, TileLinkedTank>("tanks") {
	override fun readChannel(tag: NBTTagCompound): TankChannelData {
		val deleted = tag.getBoolean("Deleted")
		val ownerUUID = tag.getUniqueId("OwnerUUID")!!
		val ownerUsername = tag.getString("OwnerUsername")
		val name = tag.getString("Name")
		val linkedPositionCount = tag.getInteger("LinkedPositionCount")
		val linkedPositions = HashSet<DimBlockPos>(linkedPositionCount)
		(0..<linkedPositionCount).mapTo(linkedPositions) {
			DimBlockPos.fromString(tag.getString("LinkedPosition$$it"))
		}
		val fluid = FluidRegistry.getFluid(tag.getString("FluidName"))
		val fluidAmount = tag.getInteger("FluidAmount")

		return TankChannelData(deleted, ownerUUID, ownerUsername, name, fluid, fluidAmount, linkedPositions)
	}

	override fun writeChannel(channelData: TankChannelData) =
		NBTTagCompound().apply {
			setBoolean("Deleted", channelData.deleted)
			setUniqueId("OwnerUUID", channelData.ownerUUID)
			setString("OwnerUsername", channelData.ownerUsername)
			setString("Name", channelData.name)
			setInteger("LinkedPositionCount", channelData.linkedPositions.size)
			channelData.linkedPositions.forEachIndexed { idx, pos ->
				setString("LinkedPosition$$idx", pos.toString())
			}
			if(channelData.fluid != null)
				setString("FluidName", FluidRegistry.getFluidName(channelData.fluid))
			setInteger("FluidAmount", channelData.fluidAmount)
		}

	override fun createEmptyChannel(player: EntityPlayer, te: TileLinkedTank, channelName: String?) =
		TankChannelData(false, player.uniqueID, player.gameProfile.name, channelName ?: "New channel $nextChannelId", null, 0, hashSetOf(te.pos dim te.world.dimId))
}
