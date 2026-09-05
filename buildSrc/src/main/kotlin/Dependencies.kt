import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.dependencies
import plugins.Logger
import util.EnumConfiguration

/**
 * Adds a dependency to the specified configuration with an option to set its transitivity.
 *
 * @param configuration The configuration to which the dependency should be added (e.g., "implementation", "compileOnly").
 * @param notation The dependency notation (e.g., "group:name:version").
 * @param transitive Whether the dependency should be transitive. Default is true.
 * @receiver The DependencyHandler to which the dependency is added.
 */
fun DependencyHandler.dep(configuration: EnumConfiguration, notation: Any, transitive: Boolean = true, changing: Boolean = false) {
    Logger.info("Adding dependency '$notation' with configuration '$configuration' (transitive=$transitive, changing=$changing)")
    val dep = add(configuration.toString(), notation)
    (dep as? ExternalModuleDependency)?.let {
        it.isTransitive = transitive
        it.isChanging = changing
    }
}

fun Project.loadDefaultDependencies() {
    dependencies {
        /**
         * Adds the dependency as an implementation dependency if the specified property is true,
         * otherwise adds it as a compileOnly dependency.
         *
         * @param run The name of the property to check.
         * @param transitive Whether the dependency should be transitive. Default is true.
         * @receiver The dependency notation to add.
         */
        fun String.dependency(run: String, transitive: Boolean = true) {
            if (propertyBoolean(run)) {
                dep(EnumConfiguration.IMPLEMENTATION, this, transitive)
            } else {
                dep(EnumConfiguration.COMPILE_ONLY, this, transitive)
            }
        }

        fun String.requiresMixins() {
            if (propertyBoolean("use_mixinbooter")) dep(EnumConfiguration.RUNTIME_ONLY, this)
        }

        dep(EnumConfiguration.COMPILE_ONLY, "org.jetbrains:annotations:${propertyString("jetbrains_annotations_version")}")
        dep(EnumConfiguration.ANNOTATION_PROCESSOR, "org.jetbrains:annotations:${propertyString("jetbrains_annotations_version")}")

        dep(EnumConfiguration.PATCHED_MINECRAFT, "net.minecraft:launchwrapper:${propertyString("launchwrapper_version")}", false)

        // Include StripLatestForgeRequirements by default for the dev env, saves everyone a hassle
        "com.cleanroommc:strip-latest-forge-requirements:${propertyString("striplatestforgerequirement_version")}".requiresMixins()
        // Include OSXNarratorBlocker by default for the dev env, for M1+ Macs
        "com.cleanroommc:osxnarratorblocker:${propertyString("osxnarratorblocker_version")}".requiresMixins()

        // Required dependencies
        "io.github.chaosunity.forgelin:Forgelin-Continuous:${propertyString("forgelin_continuous_version")}".dependency(
            "use_forgelincontinuous",
            false,
        )
        "com.cleanroommc:configanytime:${propertyString("configanytime_version")}".dependency("use_configanytime")
        "com.cleanroommc:assetmover:${propertyString("assetmover_version")}".dependency("use_assetmover")
        "com.cleanroommc:modularui:${propertyString("modularui_version")}".dependency("use_modularui", false)

        if (propertyBoolean("use_catalyx") && propertyString("mod_id") != "catalyx") {
            dep(EnumConfiguration.IMPLEMENTATION, "org.ender_development:catalyx:${propertyString("catalyx_version")}", false, true)
        }

        // Optional dependencies
        "com.cleanroommc:groovyscript:${propertyString("groovyscript_version")}".dependency("use_groovyscript", false)
        "mezz:jei:${propertyString("hei_version")}".dependency("use_hei")

        "CraftTweaker2:CraftTweaker2-API:${propertyString("crafttweaker_version")}".dependency("use_crafttweaker")
        "CraftTweaker2:ZenScript:${propertyString("crafttweaker_version")}".dependency("use_crafttweaker")
        "CraftTweaker2:CraftTweaker2-MC1120-Main:1.12-${propertyString("crafttweaker_version")}".dependency("use_crafttweaker")

        // Utilities
        dep(EnumConfiguration.RUNTIME_ONLY, "futbol.rozbrajacz:rozutils:0.4.0", false, false)
    }
}
