package org.ender_development.linkedcbt.modules

import org.ender_development.linkedcbt.LinkedCBT
import org.ender_development.linkedcbt.Tags
import org.ender_development.catalyx.api.v1.modules.annotations.CatalyxModule
import org.ender_development.catalyx.api.v1.modules.interfaces.ICatalyxModule

@CatalyxModule(
	moduleId = LCBTModuleContainer.MODULE_CORE,
	containerId = Tags.MOD_ID,
	name = "Core",
	description = "The core module required by all other modules from ${Tags.MOD_NAME}.",
	coreModule = true,
	version = Tags.VERSION
)
open class LCBTCoreModule : ICatalyxModule {
	override val logger = LinkedCBT.logger
}
