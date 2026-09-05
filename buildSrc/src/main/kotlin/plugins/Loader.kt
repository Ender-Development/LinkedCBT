package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.extensions.core.extra
import java.io.File
import java.io.FileNotFoundException
import java.util.Properties

/**
 * A property file is a simple key-value pair file used for configuration ending with .properties extension.
 * Example `example.properties` file content:
 * ```
 * # Comment line
 * key1 = value1
 * key2 = value2
 * ```
 */
typealias PropertyFile = String

/**
 * A property name is the key used to identify a specific property in a properties file.
 */
typealias PropertyName = String

/**
 * A property value is the value associated with a specific property name in a properties file.
 */
typealias PropertyValue = String

/**
 * Loader is a singleton object responsible for loading and managing properties from multiple property files.
 * It provides methods to load properties from files, retrieve specific properties, and get all loaded properties.
 */
class Loader : Plugin<Project> {
    companion object {

        private val extraProperties = mutableMapOf<PropertyFile, Map<PropertyName, PropertyValue>>()

        /**
         * Loads properties from all predefined property files.
         * The properties are stored in a map where the key is the property file name and the value is another map of property names to property values.
         * If a property file has already been loaded, it will not be loaded again.
         */
        private fun loadAllProperties() {
            listOf(
                "build.properties",
                "deps.properties",
                "integration.properties",
                "publishing.properties",
                "utilities.properties",
            ).forEach {
                loadPropertyFile(it)
            }
        }

        /**
         * Loads properties from the specified property file if they haven't been loaded already.
         * The properties are stored in a map where the key is the property name and the value is the property value.
         *
         * @param fileName The name of the properties file to load. For example, "config.properties".
         */
        internal fun loadPropertyFile(fileName: PropertyFile) {
            if (extraProperties.containsKey(fileName)) return
            val properties = loadPropertyFromFile(fileName)
            val map = properties.entries.associate { it.key.toString() to it.value.toString() }
            Logger.info("Loaded ${map.size} properties from '$fileName'")
            extraProperties[fileName] = map
        }

        /**
         * Loads properties from the specified property file and returns them as a Properties object.
         * The method first checks if the file exists in the file system; if not, it attempts to load it from the classpath.
         *
         * @param propertyFile The name of the properties file to load. For example, "config.properties".
         * @return A [Properties] object containing the loaded properties.
         * @throws FileNotFoundException if the properties file is not found in both the file system and classpath.
         */
        fun loadPropertyFromFile(propertyFile: PropertyFile): Properties {
            val properties = Properties()
            val file = File(propertyFile)
            if (file.exists()) {
                properties.load(file.inputStream())
            } else {
                this::class.java.classLoader.getResourceAsStream(propertyFile)?.let {
                    properties.load(it)
                } ?: throw FileNotFoundException("Properties file '$propertyFile' not found.")
            }
            return properties
        }

        /**
         * Retrieves the value of a specific property from a specified property file.
         *
         * @param propertyFile The name of the properties file to retrieve the property from.
         * @param propertyName The name of the property to retrieve.
         * @return The value of the property if found; otherwise, null.
         */
        internal fun getPropertyFromFile(propertyFile: PropertyFile, propertyName: PropertyName): PropertyValue? = extraProperties[propertyFile]?.get(propertyName)

        /**
         * Retrieves all loaded properties from all property files as a map of property names to property values.
         *
         * @return A map containing all loaded properties.
         */
        private fun getAllProperties(): Map<PropertyName, PropertyValue> = extraProperties.values.flatMap { it.entries }.associate { it.toPair() }
    }

    override fun apply(target: Project) {
        Logger.greet(this)

        loadAllProperties()

        getAllProperties().forEach { (key, value) ->
            target.extra.set(key, value)
        }
    }
}
