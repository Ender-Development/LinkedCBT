package io.enderdev.linkedtanks

import io.enderdev.linkedtanks.blocks.ModBlocks
import io.enderdev.linkedtanks.command.LinkedTanksCommand
import io.enderdev.linkedtanks.data.LTPersistentData
import io.enderdev.linkedtanks.items.ModItems
import io.enderdev.linkedtanks.network.PacketHandler
import net.minecraft.creativetab.CreativeTabs
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Config
import net.minecraftforge.common.config.ConfigManager
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.ender_development.catalyx.client.gui.CatalyxGuiHandler
import org.ender_development.catalyx.core.ICatalyxMod
import org.ender_development.catalyx.utils.SideUtils
import org.ender_development.catalyx.utils.extensions.toStack
import java.text.NumberFormat

@Mod(
	modid = Tags.MOD_ID,
	name = Tags.MOD_NAME,
	version = Tags.VERSION,
	dependencies = ICatalyxMod.CATALYX_ADDON,
	modLanguageAdapter = ICatalyxMod.MOD_LANGUAGE_ADAPTER
)
@Mod.EventBusSubscriber
object LinkedTanks : ICatalyxMod {
	override val creativeTab = object : CreativeTabs(Tags.MOD_ID) {
		override fun createIcon() =
			ModBlocks.linkedTank.toStack()
	}

	val guiHandler = CatalyxGuiHandler(this)
	val numberFormat: NumberFormat = NumberFormat.getNumberInstance()

	val logger: Logger = LogManager.getLogger(Tags.MOD_ID)

	@EventHandler
	fun preInit(ev: FMLPreInitializationEvent) {
		PacketHandler.init()
		NetworkRegistry.INSTANCE.registerGuiHandler(LinkedTanks, guiHandler)
		MinecraftForge.EVENT_BUS.register(this)
		if(SideUtils.isClient)
			MinecraftForge.EVENT_BUS.register(ModItems.tankConfigurator)
	}

	@EventHandler
	fun serverStarting(ev: FMLServerStartingEvent) {
		ev.registerServerCommand(LinkedTanksCommand)
	}

	@EventHandler
	fun serverStopping(ev: FMLServerStoppingEvent) {
		LTPersistentData.write()
		LTPersistentData.unload()
	}

	var lastWriteCausedBySave = 0L

	@SubscribeEvent
	fun worldSave(ev: WorldEvent.Save) {
		val currentTime = System.currentTimeMillis()
		// write to disk at most every 250ms (5t) when caused by worlds saving to hopefully avoid writing the same data n times when all n dimensions save at the same time
		if(currentTime - lastWriteCausedBySave > 250L) {
			lastWriteCausedBySave = currentTime
			LTPersistentData.write()
		}
	}

	@SubscribeEvent
	fun onConfigChangedEvent(event: ConfigChangedEvent.OnConfigChangedEvent) {
		if(event.modID == Tags.MOD_ID)
			ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE)
	}

	// because of the way Java loads classes, need to do this lol
	init {
		LTConfig.jvmLoadClass()
		ModBlocks.jvmLoadClass()
		ModItems.jvmLoadClass()
	}

	@Suppress("NOTHING_TO_INLINE")
	inline fun Int.formatNumber(): String =
		numberFormat.format(this)
}
