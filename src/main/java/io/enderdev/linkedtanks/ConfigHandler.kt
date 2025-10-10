package io.enderdev.linkedtanks

import net.minecraftforge.common.config.Config
import net.minecraftforge.common.config.ConfigManager
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID)
object ConfigHandler {
	// tank capacity
	var tankCapacity = 8000
	// basically if you add another tank to a link group, does the total link group capacity increase (like from 8B => 16B => 24B, etc.)
	var liquidStorageChangesWithTankCount = false

	@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
	object ConfigEventHandler {
		@SubscribeEvent
		@JvmStatic
		fun onConfigChangedEvent(event: ConfigChangedEvent.OnConfigChangedEvent) {
			if(event.modID == Tags.MOD_ID) {
				ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE)
			}
		}
	}
}
