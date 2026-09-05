package org.ender_development.linkedcbt.modules

import mcjty.theoneprobe.TheOneProbe
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.ender_development.catalyx.api.v1.common.Mods
import org.ender_development.catalyx.api.v1.common.extensions.subLogger
import org.ender_development.catalyx.api.v1.modules.annotations.CatalyxModule
import org.ender_development.linkedcbt.Reference
import org.ender_development.linkedcbt.integration.LTTOPIntegration

@CatalyxModule(
	moduleId = LCBTModuleContainer.MODULE_TOP,
	containerId = Reference.MODID,
	modDependencies = [Mods.TOP],
	moduleDependencies = ["${Reference.MODID}:${LCBTModuleContainer.MODULE_CORE}"],
	name = "TOP Integration",
	description = "Adds integration with The One Probe",
	version = Reference.VERSION
)
class LCBTTOPModule : LCBTCoreModule() {
	override val logger = super.logger.subLogger("TheOneProbe")

	override fun init(event: FMLInitializationEvent) {
		logger.info("TheOneProbe found. Enabling integration...")
		TheOneProbe.theOneProbeImp.registerProvider(LTTOPIntegration)
	}
}
