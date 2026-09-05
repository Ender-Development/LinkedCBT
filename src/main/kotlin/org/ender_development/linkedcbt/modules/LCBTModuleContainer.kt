package org.ender_development.linkedcbt.modules

import org.ender_development.catalyx.api.v1.modules.annotations.CatalyxModuleContainer
import org.ender_development.linkedcbt.Reference

@CatalyxModuleContainer(Reference.MODID, Reference.MODID)
object LCBTModuleContainer {
	const val MODULE_CORE = "core"
	const val MODULE_TOP = "top"
}
