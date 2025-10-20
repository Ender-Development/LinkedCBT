package io.enderdev.linkedcbt.modules

import io.enderdev.linkedcbt.Tags
import org.ender_development.catalyx.modules.CatalyxModuleContainer
import org.ender_development.catalyx.modules.ICatalyxModuleContainer

@CatalyxModuleContainer(Tags.MOD_ID)
object LTModuleContainer : ICatalyxModuleContainer {
	override val id = Tags.MOD_ID

	const val MODULE_CORE = "core"
	const val MODULE_TOP = "top"
}
