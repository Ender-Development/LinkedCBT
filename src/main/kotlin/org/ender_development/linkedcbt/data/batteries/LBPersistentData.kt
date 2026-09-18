package org.ender_development.linkedcbt.data.batteries

import org.ender_development.linkedcbt.data.DimBlockPos
import org.ender_development.linkedcbt.data.base.BasePersistentData
import org.ender_development.linkedcbt.tiles.TileLinkedBattery
import org.ender_development.linkedcbt.util.extensions.dim
import org.ender_development.linkedcbt.util.extensions.dimId
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound

object LBPersistentData : BasePersistentData<BatteryChannelData, TileLinkedBattery>("batteries") {
	override fun readChannel(tag: NBTTagCompound): BatteryChannelData {
		val ownerUUID = tag.getUniqueId("OwnerUUID")!!
		val ownerUsername = tag.getString("OwnerUsername")
		val name = tag.getString("Name")
		val linkedPositionCount = tag.getInteger("LinkedPositionCount")
		val linkedPositions = HashSet<DimBlockPos>(linkedPositionCount)
		(0..<linkedPositionCount).mapTo(linkedPositions) {
			DimBlockPos.fromString(tag.getString("LinkedPosition$$it"))
		}
		val energyAmount = tag.getInteger("EnergyAmount")
		val creationTime = tag.getLong("CreationTime")

		return BatteryChannelData(ownerUUID, ownerUsername, name, energyAmount, linkedPositions, creationTime)
	}

	override fun writeChannel(channelData: BatteryChannelData) =
		NBTTagCompound().apply {
			setUniqueId("OwnerUUID", channelData.ownerUUID)
			setString("OwnerUsername", channelData.ownerUsername)
			setString("Name", channelData.name)
			setInteger("LinkedPositionCount", channelData.linkedPositions.size)
			channelData.linkedPositions.forEachIndexed { idx, pos ->
				setString("LinkedPosition$$idx", pos.toString())
			}
			setInteger("EnergyAmount", channelData.energyAmount)
			setLong("CreationTime", channelData.creationTime)
		}

	override fun createEmptyChannel(player: EntityPlayer, te: TileLinkedBattery, channelName: String?) =
		BatteryChannelData(player.uniqueID, player.gameProfile.name, channelName ?: "New channel", 0, hashSetOf(te.pos dim te.world.dimId), System.currentTimeMillis())
}
