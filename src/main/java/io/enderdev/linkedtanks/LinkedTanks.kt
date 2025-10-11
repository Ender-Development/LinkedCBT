package io.enderdev.linkedtanks

import io.enderdev.linkedtanks.blocks.ModBlocks
import io.enderdev.linkedtanks.command.LinkedTanksCommand
import io.enderdev.linkedtanks.data.LTPersistentData
import io.enderdev.linkedtanks.network.PacketHandler
import net.minecraft.creativetab.CreativeTabs
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import org.apache.logging.log4j.Logger
import org.ender_development.catalyx.client.gui.CatalyxGuiHandler
import org.ender_development.catalyx.core.CatalyxSettings
import org.ender_development.catalyx.core.ICatalyxMod
import org.ender_development.catalyx.utils.extensions.toStack

@Mod(
	modid = Tags.MOD_ID,
	name = Tags.MOD_NAME,
	version = Tags.VERSION,
	dependencies = ICatalyxMod.CATALYX_ADDON,
	modLanguageAdapter = ICatalyxMod.MOD_LANGUAGE_ADAPTER
)
@Mod.EventBusSubscriber
object LinkedTanks : ICatalyxMod {
	val creativeTab = object : CreativeTabs(Tags.MOD_ID) {
		override fun createIcon() = ModBlocks.linkedTank.toStack()
	}

	override val modSettings = CatalyxSettings(Tags.MOD_ID, creativeTab, LinkedTanks, true)
	val guiHandler = CatalyxGuiHandler()

	lateinit var logger: Logger

	@EventHandler
	fun preInit(e: FMLPreInitializationEvent) {
		logger = e.modLog
		PacketHandler.init()
		NetworkRegistry.INSTANCE.registerGuiHandler(LinkedTanks, guiHandler)
		MinecraftForge.EVENT_BUS.register(this)
	}

	@EventHandler
	fun serverStarting(e: FMLServerStartingEvent) {
		e.registerServerCommand(LinkedTanksCommand)
	}

	//@SubscribeEvent
	//fun registerBlocks(event: RegistryEvent.Register<Block>) {
	//	ModBlocks.registerBlocks(event)
	//}
	//
	//@SubscribeEvent
	//fun registerItems(event: RegistryEvent.Register<Item>) {
	//	ModBlocks.registerItems(event)
	//	ModItems.registerItems(event)
	//}

	@EventHandler
	fun serverStopping(event: FMLServerStoppingEvent) {
		LTPersistentData.write()
	}

	var lastWriteCausedBySave = 0L

	@SubscribeEvent
	fun worldSave(event: WorldEvent.Save) {
		println("--- WORLDEVENT.SAVE CALLED ---")
		val currentTime = System.currentTimeMillis()
		// write to disk at most every 250ms (5t) when caused by worlds saving to hopefully avoid writing the same data n times when all n dimensions save at the same time
		if(currentTime - lastWriteCausedBySave > 250L) {
			lastWriteCausedBySave = currentTime
			LTPersistentData.write()
		}
	}

	// because of the way Java loads classes, need to do this lol
	init {
		ModBlocks.hi()
	}

	// TODO linkedtanks command for ops to manage stuff
	// TODO recipes
}
