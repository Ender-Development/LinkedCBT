package util

enum class EnumProvider(val shortName: String) {
    CURSEFORGE("CF"),
    MODRINTH("MR"),
    MAVEN("MV"),
}

fun String.toProvider(): EnumProvider? = EnumProvider.values().firstOrNull { it.shortName == this }

@Suppress("unused")
enum class EnumConfiguration(val configurationName: String) {
    /**
     * If you need this for internal implementation details of the mod, but none of it is visible via the public API
     * Available at runtime but not compile time for mods depending on this mod
     */
    IMPLEMENTATION("implementation"),

    /**
     * If the mod you're building doesn't need this dependency during runtime at all, e.g. for optional mods
     * Not available at all for mods depending on this mod, only visible at compile time for this mod
     */
    COMPILE_ONLY("compileOnly"),

    /**
     * If you don't need this at compile time, but want it to be present at runtime
     * Available at runtime for mods depending on this mod
     */
    RUNTIME_ONLY("runtimeOnly"),

    /**
     *  Mostly for java compiler plugins, if you know you need this, use it, otherwise don't worry
     */
    ANNOTATION_PROCESSOR("annotationProcessor"),

    /**
     *  If you want to embed this dependency into your mod jar
     *  NOT RECOMMENDED unless you absolutely have to
     */
    EMBED("embed"),

    /**
     *  Special configuration for patched Minecraft dependencies
     *  ONLY FOR INTERNAL USE. DO NOT USE THIS IN YOUR MODS
     */
    PATCHED_MINECRAFT("patchedMinecraft"),
    ;

    override fun toString() = configurationName
}

fun String.toConfiguration(): EnumConfiguration? = EnumConfiguration.values().firstOrNull { it.configurationName.lowercase().trim() == this.lowercase().trim() }

typealias ModSource = String
typealias isTransitive = Boolean
typealias isChanging = Boolean

data class ModDependency(val source: ModSource, val configuration: EnumConfiguration, val enabled: Boolean, val transitive: isTransitive, val changing: isChanging)

abstract class AbstractDependency(val enabled: Boolean, private val configuration: String?, private val transitive: Boolean?, private val changing: Boolean?) {
    abstract override fun toString(): String

    /**
     * Indicates whether the dependency is transitive.
     * Default is true if not explicitly set to false.
     */
    fun transitive(): Boolean = transitive != false

    /**
     * Indicates whether the dependency is changing.
     * Default is false if not explicitly set to true.
     */
    fun changing(): Boolean = changing == true

    fun modDependency(): ModDependency = ModDependency(toString(), configuration(), enabled, transitive(), changing())

    fun configuration(): EnumConfiguration = configuration?.toConfiguration() ?: if (enabled) EnumConfiguration.IMPLEMENTATION else EnumConfiguration.COMPILE_ONLY
}

class Maven(val group: String, val artifact: String, val version: String, enabled: Boolean, configuration: String?, transitive: Boolean?, changing: Boolean?) :
    AbstractDependency(enabled, configuration, transitive, changing) {
    override fun toString() = "$group:$artifact:$version"
}

class Modrinth(val projectId: String, val fileId: String, enabled: Boolean, configuration: String?, transitive: Boolean?, changing: Boolean?) : AbstractDependency(enabled, configuration, transitive, changing) {
    override fun toString() = "maven.modrinth:$projectId:$fileId"
}

class Curseforge(val projectName: String, val projectId: String, val fileId: String, enabled: Boolean, configuration: String?, transitive: Boolean?, changing: Boolean?) :
    AbstractDependency(enabled, configuration, transitive, changing) {
    override fun toString() = "curse.maven:$projectName-$projectId:$fileId"
}
