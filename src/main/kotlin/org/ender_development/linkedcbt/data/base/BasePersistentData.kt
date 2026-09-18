package org.ender_development.linkedcbt.data.base

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import org.ender_development.catalyx.core.common.persistence.WorldPersistentData
import org.ender_development.linkedcbt.LinkedCBT
import org.ender_development.linkedcbt.Reference
import org.ender_development.linkedcbt.tiles.BaseLinkedTile
import java.util.*

abstract class BasePersistentData<CH_DATA : BaseChannelData<CH_DATA, *>, TE : BaseLinkedTile<TE, CH_DATA, *, *>>(type: String) {
	protected val dataNBT = WorldPersistentData(ResourceLocation(Reference.MODID, type), true, ::read, ::unload)
	private var wasRead = false
	val data = hashMapOf<UUID, CH_DATA>()

	fun read() {
		if(wasRead)
			return

		wasRead = true

		data.clear()

		dataNBT.data.keySet.forEach { key ->
			val tag = dataNBT.data.getCompoundTag(key)
			data[UUID.fromString(key)] = readChannel(tag)
		}
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

	fun createNewChannel(player: EntityPlayer, te: TE, channelName: String? = null): UUID {
		val channelData = createEmptyChannel(player, te, channelName)
		val channelId = UUID.randomUUID()
		data[channelId] = channelData
		return channelId
	}

	protected abstract fun createEmptyChannel(player: EntityPlayer, te: TE, channelName: String?): CH_DATA

	init {
		// try to load the data
		dataNBT.data
	}
}
