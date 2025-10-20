package io.enderdev.linkedcbt.data.tanks

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.Tags
import io.enderdev.linkedcbt.data.DimBlockPos
import io.enderdev.linkedcbt.tiles.TileLinkedTank
import io.enderdev.linkedcbt.util.extensions.dim
import io.enderdev.linkedcbt.util.extensions.dimId
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.FluidRegistry
import org.ender_development.catalyx.utils.persistence.WorldPersistentData

object LTPersistentData {
	private val dataNBT = WorldPersistentData(ResourceLocation(Tags.MOD_ID, "tanks"), true, ::read, ::unload)
	private var wasRead = false
	private var nextChannelId = 1
	val data: Int2ObjectMap<TankChannelData> = Int2ObjectOpenHashMap()

	fun read() {
		if(wasRead)
			return

		wasRead = true

		data.clear()

		var nextUnbrokenId = 1
		dataNBT.data.keySet.sortedBy(String::toInt).forEach {
			if(it.toInt() == nextUnbrokenId)
				++nextUnbrokenId
			val tag = dataNBT.data.getCompoundTag(it)
			val deleted = tag.getBoolean("Deleted")
			val ownerUUID = tag.getUniqueId("OwnerUUID") ?: return@forEach
			val ownerUsername = tag.getString("OwnerUsername")
			val name = tag.getString("Name")
			val linkedPositionCount = tag.getInteger("LinkedPositionCount")
			val linkedPositions = HashSet<DimBlockPos>(linkedPositionCount)
			(0..<linkedPositionCount).mapTo(linkedPositions) {
				DimBlockPos.Companion.fromString(tag.getString("LinkedPosition$$it"))
			}
			val fluid = FluidRegistry.getFluid(tag.getString("FluidName"))
			val fluidAmount = tag.getInteger("FluidAmount")

			data.put(it.toInt(), TankChannelData(deleted, ownerUUID, ownerUsername, name, fluid, fluidAmount, linkedPositions))
		}

		nextChannelId = nextUnbrokenId
	}

	// I'd have to write() every IFluidHandler operation, instead of saving every modification, try to save on exit/ServerStopping/WorldSave/… and accept the unlikely data loss
	fun write() {
		if(!wasRead)
			return

		// lazy option, if performance requires it I might need to optimise this more
		dataNBT.data.tagMap.clear()

		data.entries.forEach { (channelId, channelData) ->
			dataNBT.data.setTag(channelId.toString(), NBTTagCompound().apply {
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
			})
		}
		LinkedCBT.logger.debug("Saving data to disk: {}", dataNBT.data)
		dataNBT.save()
	}

	fun unload() {
		write()
		wasRead = false
		data.clear()
	}

	fun createNewChannel(player: EntityPlayer, te: TileLinkedTank, channelName: String? = null): Int {
		val channelData = TankChannelData(false, player.uniqueID, player.gameProfile.name, channelName ?: "New channel $nextChannelId", null, 0, hashSetOf(te.pos dim te.world.dimId))
		data.put(nextChannelId, channelData)
		return nextChannelId.also {
			nextChannelId = findNextFreeChannelId()
		}
	}

	private fun findNextFreeChannelId(): Int {
		var id = 1
		for(it in data.keys.sorted())
			if(it == id)
				++id
			else
				break
		return id
	}

	init {
		// try to load the data
		dataNBT.data
	}
}
