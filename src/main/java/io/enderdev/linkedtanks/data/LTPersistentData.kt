package io.enderdev.linkedtanks.data

import io.enderdev.linkedtanks.LinkedTanks
import io.enderdev.linkedtanks.Tags
import io.enderdev.linkedtanks.tiles.TileLinkedTank
import io.enderdev.linkedtanks.util.extensions.dim
import io.enderdev.linkedtanks.util.extensions.dimId
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids.FluidRegistry
import org.ender_development.catalyx.utils.PersistentData

object LTPersistentData {
	private val dataNBT = PersistentData(Tags.MOD_ID)
	private var wasRead = false
	private var nextChannelId = 0
	private val dataMap: Int2ObjectMap<ChannelData> = Int2ObjectOpenHashMap()

	fun read() {
		if(wasRead)
			return

		wasRead = true

		dataMap.clear()

		var nextUnbrokenId = 1
		dataNBT.tag.keySet.sortedBy(String::toInt).forEach {
			if(it.toInt() == nextUnbrokenId)
				++nextUnbrokenId
			val tag = dataNBT.tag.getCompoundTag(it)
			val deleted = tag.getBoolean("Deleted")
			val ownerUUID = tag.getUniqueId("OwnerUUID") ?: return@forEach
			val ownerUsername = tag.getString("OwnerUsername")
			val name = tag.getString("Name")
			val linkedPositionCount = tag.getInteger("LinkedPositionCount")
			val linkedPositions = HashSet<DimBlockPos>(linkedPositionCount)
			(0..<linkedPositionCount).mapTo(linkedPositions) {
				DimBlockPos.fromString(tag.getString("LinkedPosition$$it"))
			}
			val fluid = FluidRegistry.getFluid(tag.getString("FluidName"))
			val fluidAmount = tag.getInteger("FluidAmount")

			dataMap.put(it.toInt(), ChannelData(deleted, ownerUUID, ownerUsername, name, fluid, fluidAmount, linkedPositions))
		}

		nextChannelId = nextUnbrokenId
	}

	// I'd have to write() every IFluidHandler operation, instead of saving every modification, try to save on exit/ServerStopping/WorldSave/… and accept the unlikely data loss
	fun write() {
		if(!wasRead)
			return

		// lazy option, if performance requires it I might need to optimise this more
		dataNBT.tag.tagMap.clear()

		dataMap.entries.forEach { (channelId, channelData) ->
			dataNBT.tag.setTag(channelId.toString(), NBTTagCompound().apply {
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
		LinkedTanks.logger.debug("Saving data to disk: {}", dataNBT.tag)
		dataNBT.save()
	}

	fun unload() {
		dataMap.clear()
		wasRead = false
	}

	val data: Int2ObjectMap<ChannelData>
		get() {
			read()
			return dataMap
		}

	fun createNewChannel(player: EntityPlayer, te: TileLinkedTank): Int {
		val channelData = ChannelData(false, player.uniqueID, player.gameProfile.name, "New channel $nextChannelId", null, 0, hashSetOf(te.pos dim te.world.dimId))
		dataMap.put(nextChannelId, channelData)
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
}
