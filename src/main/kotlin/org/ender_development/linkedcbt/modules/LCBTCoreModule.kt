package org.ender_development.linkedcbt.modules

import org.ender_development.catalyx.api.v1.modules.annotations.CatalyxModule
import org.ender_development.catalyx.api.v1.modules.interfaces.ICatalyxModule
import org.ender_development.linkedcbt.LinkedCBT
import org.ender_development.linkedcbt.Reference

@CatalyxModule(
	moduleId = LCBTModuleContainer.MODULE_CORE,
	containerId = Reference.MODID,
	name = "Core",
	description = "The core module required by all other modules from ${Reference.MOD_NAME}.",
	coreModule = true,
	version = Reference.VERSION
)
open class LCBTCoreModule : ICatalyxModule {
	override val logger = LinkedCBT.logger
}
