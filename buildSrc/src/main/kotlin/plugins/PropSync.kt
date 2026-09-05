package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import util.OnlineUtils
import util.OnlineUtils.isOnline
import util.OnlineUtils.shouldDisableSync
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties

class PropSync : Plugin<Project> {
    data class SyncConfig(val keysToSync: List<String> = emptyList(), val syncAll: Boolean = false, val path: String = "buildSrc/src/main/resources/")

    companion object {
        private var foundUpdate = false
        private lateinit var project: Project

        private val syncConfig: Map<PropertyFile, SyncConfig> = mapOf(
            "build.properties" to SyncConfig(
                keysToSync = listOf(
                    "jetbrains_annotations_version",
                    "launchwrapper_version",
                    "striplatestforgerequirement_version",
                    "osxnarratorblocker_version",
                ),
            ),
            "deps.properties" to SyncConfig(
                keysToSync = listOf(
                    "assetmover_version",
                    "catalyx_version",
                    "configanytime_version",
                    "forgelin_continuous_version",
                    "mixin_booter_version",
                    "modularui_version",
                ),
            ),
            "integration.properties" to SyncConfig(
                keysToSync = listOf("crafttweaker_version", "groovyscript_version", "hei_version", "top_version"),
            ),
            "utilities.properties" to SyncConfig(
                keysToSync = listOf("ktlint_version", "google_java_format_version", "flexmark_version"),
            ),
            "tags.properties" to SyncConfig(
                syncAll = true,
            ),
            // This one is new, still needs to be tested
            "gradle.properties" to SyncConfig(
                keysToSync = listOf("kotlin_version"),
                path = "",
            ),
        )

        fun syncPropertiesFromTemplate() {
            Logger.banner("Searching for Properties to sync!")
            if (shouldDisableSync()) return Logger.info("Sync is disabled via system.")
            if (!isOnline()) return Logger.warn("No internet connection detected.")
            performSync()
        }

        private fun performSync() {
            Logger.info("Syncing with template repository: ${OnlineUtils.TEMPLATE_REPO}")
            syncConfig.forEach { (file, cfg) ->
                try {
                    val templateProperties = fetchTemplateProperties(file, cfg.path)
                    val localProperties = Loader.loadPropertyFromFile(file)

                    val mergedProperties = mergeProperties(localProperties, templateProperties, cfg)
                    if (foundUpdate) {
                        updateLocalPropertiesFile(cfg.path, file, mergedProperties)
                        Logger.info("Synchronized properties for '$file'")
                    } else {
                        Logger.info("No changes detected for '$file'")
                    }
                } catch (e: Exception) {
                    Logger.error("Failed to sync properties for '$file': ${e.message}")
                }
            }
        }

        private fun fetchTemplateProperties(fileName: PropertyFile, path: String): Properties {
            val url = "${OnlineUtils.GITHUB_RAW_URL}/${OnlineUtils.TEMPLATE_REPO}/${OnlineUtils.TEMPLATE_BRANCH}/$path$fileName"
            val properties = Properties()
            val content = OnlineUtils.fetchFileContent(url) ?: throw Exception("Failed to fetch content from $url")
            properties.load(ByteArrayInputStream(content.toByteArray()))
            Logger.info("Fetched template properties from '$url'")
            return properties
        }

        private fun mergeProperties(local: Properties, remote: Properties, cfg: SyncConfig): Properties {
            val merged = Properties()
            local.forEach { (key, value) -> merged[key] = value }
            if (cfg.syncAll) {
                remote.forEach { (key, value) ->
                    if (local.getProperty(key.toString()) != value.toString()) {
                        Logger.info("Updating property '$key': ${local.getProperty(key.toString())} -> '$value'")
                        foundUpdate = true
                    }
                    merged[key] = value
                }
            } else {
                cfg.keysToSync.forEach { key ->
                    remote.getProperty(key)?.let { value ->
                        val localValue = local.getProperty(key)
                        if (localValue != value) {
                            Logger.info("Updating property '$key': $localValue -> $value")
                            merged[key] = value
                            foundUpdate = true
                        }
                    }
                }
            }
            return merged
        }

        private fun updateLocalPropertiesFile(path: String, fileName: PropertyFile, properties: Properties) {
            val buildSrcDirectory = project.rootProject.file(path.removeSuffix("/"))
            val propertyFile = File(buildSrcDirectory, fileName)
            if (propertyFile.exists().not()) {
                return Logger.warn("Property file '$fileName' does not exist at ${propertyFile.absolutePath}, skipping update.")
            }

            try {
                val backupFile = File(propertyFile.parent, "$fileName.bak")
                Files.copy(propertyFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                writePropertiesToFile(propertyFile, properties)
            } catch (e: Exception) {
                Logger.error("Error updating local properties file '$fileName': $e")
            }
        }

        private fun writePropertiesToFile(file: File, properties: Properties) {
            val lines = mutableListOf<String>()
            if (file.exists()) {
                val existingLines = file.readLines()
                val updatedKeys = mutableSetOf<String>()

                existingLines.forEach {
                    when {
                        it.startsWith("#") || it.trim().isEmpty() -> lines.add(it)

                        it.contains("=") -> {
                            val key = it.substringBefore("=").trim()
                            val newValue = properties.getProperty(key)
                            newValue?.let {
                                if (newValue.isEmpty()) {
                                    lines.add("$key =")
                                } else {
                                    lines.add("$key = $newValue")
                                }
                                updatedKeys.add(key)
                            } ?: lines.add(it) // Keep existing line if key not in new properties
                        }

                        else -> lines.add(it)
                    }
                }
                // Add any new properties that were not in the existing file
                properties.forEach { (key, value) ->
                    if (key.toString() !in updatedKeys) {
                        lines.add("$key = $value")
                    }
                }
            } else {
                Logger.warn("File '${file.name}' does not exist. Creating a new one.")
                lines.add("# Properties file created by Sync plugin on ${java.time.LocalDateTime.now()}")
                properties.forEach { (key, value) ->
                    lines.add("$key = $value")
                }
            }
            val separator = System.lineSeparator()
            file.writeText(lines.joinToString(separator).plus(separator))
        }
    }

    override fun apply(target: Project) {
        project = target
        Logger.greet(this)
    }
}
