package io.enderdev.linkedtanks.data

import io.enderdev.linkedtanks.ConfigHandler
import io.enderdev.linkedtanks.Tags
import io.enderdev.linkedtanks.data.LTPersistentData.DimBlockPos.Companion.dim
import io.enderdev.linkedtanks.tiles.TileLinkedTank
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import org.ender_development.catalyx.utils.PersistentData
import java.util.*

object LTPersistentData {
	private val dataNBT = PersistentData(Tags.MOD_ID)
	private var wasRead = false
	private var nextChannelId = 0
	private val dataMap: Int2ObjectMap<ChannelData> = Int2ObjectOpenHashMap()

	fun read() {
		if(wasRead)
			return

		wasRead = true

		dataNBT.tag.keySet.forEach {
			val tag = dataNBT.tag.getCompoundTag(it)
			val ownerUUID = tag.getUniqueId("OwnerUUID") ?: return@forEach
			val ownerUsername = tag.getString("OwnerUsername")
			val name = tag.getString("Name")
			val linkedPositionCount = tag.getInteger("LinkedPositionCount")
			val linkedPositions = HashSet<DimBlockPos>(linkedPositionCount)
			repeat(linkedPositionCount) {
				DimBlockPos.fromString(tag.getString("LinkedPosition$$it"))
			}
			val fluid = FluidRegistry.getFluid(tag.getString("FluidName"))
			val fluidAmount = tag.getInteger("FluidAmount")

			dataMap.put(it.toInt(), ChannelData(ownerUUID, ownerUsername, name, fluid, fluidAmount, linkedPositions))
		}

		nextChannelId = (dataMap.keys.maxOrNull() ?: 0) + 1
	}

	// I'd have to write() every IFluidHandler operation, instead of saving every modification, try to save on exit/ServerStopping/WorldSave/… and accept the unlikely data loss
	fun write() {
		println("----- LTPersistentData#write called, saving all data -----")
		// lazy option, if performance requires it I might need to optimise this more
		dataNBT.tag.tagMap.clear()

		dataMap.entries.forEach { (channelId, channelData) ->
			dataNBT.tag.setTag(channelId.toString(), NBTTagCompound().apply {
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
		println(dataNBT.tag)
		dataNBT.save()
	}

	val data: Int2ObjectMap<ChannelData>
		get() {
			read()
			return dataMap
		}

	fun createNewChannel(player: EntityPlayer, te: TileLinkedTank): Int {
		val channelData = ChannelData(player.uniqueID, player.gameProfile.name, "New Channel $nextChannelId", null, 0, hashSetOf(te.pos dim te.world.dimId))
		dataMap.put(nextChannelId, channelData)
		return nextChannelId++
	}

	// TODO cap `name` to a max length of like 32-48 chars
	/** **Do NOT** use [ownerUsername] nor [name] for any checking */
	data class ChannelData(val ownerUUID: UUID, var ownerUsername: String, var name: String, var fluid: Fluid?, var fluidAmount: Int, val linkedPositions: HashSet<DimBlockPos>) {
		/**
		 * Used clientside to make stuff display properly
		 */
		var fluidCapacityOverride = 0

		val fluidCapacity: Int
			get() = if(fluidCapacityOverride != 0)
				fluidCapacityOverride
			else
				ConfigHandler.tankCapacity * if(ConfigHandler.liquidStorageChangesWithTankCount) linkedPositions.size else 1
	}

	data class DimBlockPos(val dimId: Int, val pos: BlockPos) {
		constructor(dimId: Int, x: Int, y: Int, z: Int) : this(dimId, BlockPos(x, y, z))

		val x: Int
			inline get() = pos.x

		val y: Int
			inline get() = pos.y

		val z: Int
			inline get() = pos.z

		val world: WorldServer?
			inline get() = DimensionManager.getWorld(dimId)

		override fun toString() =
			"$dimId;$x;$y;$z"

		companion object {
			fun fromString(str: String): DimBlockPos {
				val split = str.split(';')
				return DimBlockPos(split[0].toInt(), split[1].toInt(), split[2].toInt(), split[3].toInt())
			}

			@Suppress("NOTHING_TO_INLINE")
			inline infix fun BlockPos.dim(dimId: Int) =
				DimBlockPos(dimId, this)
		}
	}

	// TODO verify if this is correct; alternatively can always do `DimensionManager.getWorldIDs().find { DimensionManager.getWorld(it) === this }`
	val World.dimId: Int
		inline get() = provider.dimensionType.id
}
