package io.enderdev.linkedcbt.modules

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.Tags
import org.ender_development.catalyx.modules.CatalyxModule
import org.ender_development.catalyx.modules.ICatalyxModule

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
