package io.enderdev.linkedcbt

import io.enderdev.linkedcbt.blocks.ModBlocks
import io.enderdev.linkedcbt.command.LinkedCBTCommand
import io.enderdev.linkedcbt.data.batteries.LBPersistentData
import io.enderdev.linkedcbt.data.chests.LCPersistentData
import io.enderdev.linkedcbt.data.tanks.LTPersistentData
import io.enderdev.linkedcbt.items.ModItems
import io.enderdev.linkedcbt.network.PacketHandler
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
object LinkedCBT : ICatalyxMod {
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
		NetworkRegistry.INSTANCE.registerGuiHandler(LinkedCBT, guiHandler)
		MinecraftForge.EVENT_BUS.register(this)
		if(SideUtils.isClient)
			MinecraftForge.EVENT_BUS.register(ModItems.sideConfigurator)
	}

	@EventHandler
	fun serverStarting(ev: FMLServerStartingEvent) {
		ev.registerServerCommand(LinkedCBTCommand)
	}

	var lastWriteCausedBySave = 0L

	@SubscribeEvent
	fun worldSave(ev: WorldEvent.Save) {
		val currentTime = System.currentTimeMillis()
		// write to disk at most every 500ms when caused by worlds saving to hopefully avoid writing the same data n times when all n dimensions save at the same time
		if(currentTime - lastWriteCausedBySave > 500L) {
			lastWriteCausedBySave = currentTime
			LCPersistentData.write()
			LBPersistentData.write()
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
		LCBTConfig.jvmLoadClass()
		ModBlocks.jvmLoadClass()
		ModItems.jvmLoadClass()
	}
}
