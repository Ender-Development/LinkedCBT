package io.enderdev.linkedcbt.modules

import io.enderdev.linkedcbt.Tags
import io.enderdev.linkedcbt.integration.LTTOPIntegration
import mcjty.theoneprobe.TheOneProbe
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.ender_development.catalyx.integration.Mods
import org.ender_development.catalyx.modules.CatalyxModule
import org.ender_development.catalyx.utils.extensions.subLogger

@CatalyxModule(
	moduleId = LCBTModuleContainer.MODULE_TOP,
	containerId = Tags.MOD_ID,
	modDependencies = [Mods.TOP],
	moduleDependencies = ["${Tags.MOD_ID}:${LCBTModuleContainer.MODULE_CORE}"],
	name = "TOP Integration",
	description = "Adds integration with The One Probe",
	version = Tags.VERSION
)
class LCBTTOPModule : LCBTCoreModule() {
	override val logger = super.logger.subLogger("TheOneProbe")

	override fun init(event: FMLInitializationEvent) {
		logger.info("TheOneProbe found. Enabling integration...")
		TheOneProbe.theOneProbeImp.registerProvider(LTTOPIntegration)
	}
}
