package io.enderdev.linkedcbt.data.base

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.Tags
import io.enderdev.linkedcbt.tiles.BaseLinkedTile
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import org.ender_development.catalyx.utils.persistence.WorldPersistentData

abstract class BasePersistentData<CH_DATA : BaseChannelData<CH_DATA, *>, TE : BaseLinkedTile<TE, CH_DATA, *, *>>(type: String) {
	protected val dataNBT = WorldPersistentData(ResourceLocation(Tags.MOD_ID, type), true, ::read, ::unload)
	private var wasRead = false
	protected var nextChannelId = 1
	val data: Int2ObjectMap<CH_DATA> = Int2ObjectOpenHashMap()

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
			data.put(it.toInt(), readChannel(tag))
		}

		nextChannelId = nextUnbrokenId
	}

	protected abstract fun readChannel(tag: NBTTagCompound): CH_DATA

	fun write() {
		if(!wasRead)
			return

		// lazy option, if performance requires it I might need to optimise this more
		dataNBT.data.tagMap.clear()

		data.entries.forEach { (channelId, channelData) ->
			dataNBT.data.setTag(channelId.toString(), writeChannel(channelData))
		}
		LinkedCBT.logger.debug("Saving data to disk: {}", dataNBT.data)
		dataNBT.save()
	}

	protected abstract fun writeChannel(channelData: CH_DATA): NBTTagCompound

	private fun unload() {
		write()
		wasRead = false
		data.clear()
	}

	fun createNewChannel(player: EntityPlayer, te: TE, channelName: String? = null): Int {
		val channelData = createEmptyChannel(player, te, channelName)
		data.put(nextChannelId, channelData)
		return nextChannelId.also {
			nextChannelId = findNextFreeChannelId()
		}
	}

	protected abstract fun createEmptyChannel(player: EntityPlayer, te: TE, channelName: String?): CH_DATA

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
