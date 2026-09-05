package org.ender_development.linkedcbt.modules

import org.ender_development.linkedcbt.Tags
import org.ender_development.linkedcbt.integration.LTTOPIntegration
import mcjty.theoneprobe.TheOneProbe
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.ender_development.catalyx.api.v1.common.Mods
import org.ender_development.catalyx.api.v1.common.extensions.subLogger
import org.ender_development.catalyx.api.v1.modules.annotations.CatalyxModule

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
