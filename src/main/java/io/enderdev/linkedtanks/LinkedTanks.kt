package io.enderdev.linkedtanks

import io.enderdev.linkedtanks.blocks.ModBlocks
import net.minecraft.creativetab.CreativeTabs
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
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

	// TODO linkedtanks command for ops to manage stuff
	// TODO recipes
}
