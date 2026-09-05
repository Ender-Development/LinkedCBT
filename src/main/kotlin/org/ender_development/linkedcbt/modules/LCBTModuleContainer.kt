package org.ender_development.linkedcbt.modules

import org.ender_development.linkedcbt.Tags
import org.ender_development.catalyx.api.v1.modules.annotations.CatalyxModuleContainer

@CatalyxModuleContainer(Tags.MOD_ID, Tags.MOD_ID)
object LCBTModuleContainer {
	const val MODULE_CORE = "core"
	const val MODULE_TOP = "top"
}
