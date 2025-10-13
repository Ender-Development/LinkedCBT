package io.enderdev.linkedtanks.modules

import io.enderdev.linkedtanks.Tags
import org.ender_development.catalyx.modules.CatalyxModuleContainer
import org.ender_development.catalyx.modules.ICatalyxModuleContainer

@CatalyxModuleContainer
object LTModuleContainer : ICatalyxModuleContainer {
	override val id = Tags.MOD_ID

	const val MODULE_CORE = "core"
	const val MODULE_TOP = "top"
}
