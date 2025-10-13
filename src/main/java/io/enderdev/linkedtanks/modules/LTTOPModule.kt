package io.enderdev.linkedtanks.modules

import io.enderdev.linkedtanks.LinkedTanks
import io.enderdev.linkedtanks.Tags
import io.enderdev.linkedtanks.integration.LTTOPIntegration
import it.unimi.dsi.fastutil.objects.ObjectSets
import mcjty.theoneprobe.TheOneProbe
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.ender_development.catalyx.integration.Mods
import org.ender_development.catalyx.modules.CatalyxModule
import org.ender_development.catalyx.modules.ICatalyxModule

@CatalyxModule(
	moduleID = LTModuleContainer.MODULE_TOP,
	containerID = Tags.MOD_ID,
	modDependencies = [Mods.TOP],
	name = "${Tags.MOD_NAME} The One Probe Integration Module",
	description = "Adds integration with The One Probe"
)
class LTTOPModule : ICatalyxModule {
	override val logger = LinkedTanks.logger

	override val dependencyUids: Set<ResourceLocation> =
		ObjectSets.singleton(ResourceLocation(Tags.MOD_ID, LTModuleContainer.MODULE_CORE))

	override fun init(event: FMLInitializationEvent) {
		logger.info("TheOneProbe found. Enabling integration...")
		TheOneProbe.theOneProbeImp.registerProvider(LTTOPIntegration)
	}
}
