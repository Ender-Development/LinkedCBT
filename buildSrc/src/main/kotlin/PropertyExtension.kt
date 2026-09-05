import groovy.lang.GroovyShell
import org.gradle.api.GradleException
import org.gradle.api.Project
import plugins.PropertyName
import plugins.PropertyValue
import plugins.Secrets

/**
 * Checks if a property with the given name exists in the project's properties.
 * If the property does not exist, a GradleException is thrown.
 *
 * @param propertyName The name of the property to check.
 * @throws GradleException if the property does not exist.
 */
fun Project.checkPropertyExists(propertyName: PropertyName) {
    if (!project.hasProperty(propertyName)) {
        throw GradleException("Property '$propertyName' not found in project properties.")
    }
}

/**
 * Checks if a property with the given name exists in the project's properties.
 * If the property exists and its value is true, it checks for the existence of all specified sub-properties.
 * If any property does not exist, a GradleException is thrown.
 *
 * @param propertyName The name of the main property to check.
 * @param subProperties The names of the sub-properties to check if the main property is true.
 * @throws GradleException if any property does not exist.
 */
fun Project.checkSubPropertiesExist(propertyName: PropertyName, vararg subProperties: PropertyName) {
    checkPropertyExists(propertyName)
    if (propertyBoolean(propertyName)) subProperties.forEach { checkPropertyExists(it) }
}

/**
 * Retrieves the value of a property, interpolating any placeholders in the format `${propertyName}`.
 * If the property does not exist, it returns null.
 *
 * @param propertyName The name of the property to retrieve.
 * @return The value of the property with placeholders interpolated.
 * @throws GradleException if the property does not exist.
 */
private fun Project.evalProperty(propertyName: PropertyName): Any? {
    checkPropertyExists(propertyName)
    val value = project.findProperty(propertyName)
    return if (value is String) evaluate(value) else value
}

/**
 * Evaluates expressions in the format `${{expression}}` within the given string.
 * Expressions are evaluated using the Kotlin scripting engine.
 * Expressions can contain property placeholders in the format `${propertyName}` which will be replaced before evaluation.
 *
 * @param value The string containing expressions to evaluate.
 * @return The string with all expressions evaluated and replaced by their results.
 * @throws GradleException if an expression fails to evaluate.
 */
fun Project.evaluate(value: PropertyValue): PropertyValue {
    if (value.startsWith("\${{") && value.endsWith("}}")) {
        val expression = placeHolder(value.substring(3, value.length - 2).trim())
        return try {
            val eval = GroovyShell().evaluate(expression).toString()
            project.logger.info("Evaluated expression '$expression' to '$eval'")
            eval
        } catch (e: Exception) {
            throw GradleException("Failed to evaluate expression '$expression' in property evaluation.", e)
        }
    }
    return placeHolder(value)
}

/**
 * Replaces placeholders in the format `${propertyName}` within the given string with their corresponding property values.
 *
 * @param value The string containing placeholders to replace.
 * @return The string with all placeholders replaced by their property values.
 */
private fun Project.placeHolder(value: PropertyValue): PropertyValue {
    var result = value
    val template = "\\$\\{([^}]+)}".toRegex()
    template.findAll(value).forEach { matchResult ->
        val placeholder = matchResult.value
        val key = matchResult.groupValues[1]
        val replacement = propertyString(key)
        result = result.replace(placeholder, replacement)
    }
    return result
}

/**
 * Retrieves the value of a property as a String.
 * If the property does not exist, a GradleException is thrown.
 *
 * @param propertyName The name of the property to retrieve.
 * @return The value of the property as a String.
 * @throws GradleException if the property does not exist.
 */
fun Project.propertyString(propertyName: PropertyName): PropertyValue = evalProperty(propertyName).toString()

/**
 * Retrieves the value of a property as a List of Strings, split by the specified delimiter.
 * If the property does not exist, a GradleException is thrown.
 *
 * @param propertyName The name of the property to retrieve.
 * @param delimiter The delimiter to use for splitting the property value. Default is a space (" ").
 * @return The value of the property as a List of Strings.
 * @throws GradleException if the property does not exist.
 */
fun Project.propertyStringList(propertyName: PropertyName, delimiter: String = " "): MutableList<PropertyValue> = propertyString(propertyName).split(delimiter).filter { it.isNotEmpty() }.toMutableList()

/**
 * Retrieves the value of a property as a Boolean.
 * If the property does not exist, a GradleException is thrown.
 *
 * @param propertyName The name of the property to retrieve.
 * @return The value of the property as a Boolean.
 * @throws GradleException if the property does not exist.
 */
fun Project.propertyBoolean(propertyName: PropertyName): Boolean = propertyString(propertyName).toBoolean()

/**
 * Sets a default value for a property if it is not already set or is empty.
 *
 * @param propertyName The name of the property to check and potentially set.
 * @param defaultValue The default value to set if the property is not set or is empty.
 */
fun Project.propertyDefaultIfUnset(propertyName: PropertyName, defaultValue: Any?) {
    if (!project.hasProperty(propertyName) || project.property(propertyName).toString().isEmpty()) {
        project.extensions.extraProperties.set(propertyName, defaultValue)
    }
}

/**
 * Sets a property to the value of an environment variable if it exists; otherwise, sets it to a default value.
 * It also checks [secrets.properties](https://github.com/stnwtr/gradle-secrets-plugin) for the environment variable.
 *
 * @param propertyName The name of the property to set.
 * @param envVarName The name of the environment variable to check.
 * @param defaultValue The default value to set if the environment variable is not set.
 */
fun Project.propertyDefaultIfUnsetWithEnvVar(propertyName: PropertyName, envVarName: String, defaultValue: Any?) {
    // Searches in the 'secrets.properties' first. If not found in the file, it checks the environment variables.
    // If neither is found it will return null.
    val envVarValue = Secrets.getOrEnvironment(envVarName)
    envVarValue?.let {
        if (it.isNotEmpty()) {
            project.extensions.extraProperties.set(propertyName, it)
        } else {
            propertyDefaultIfUnset(propertyName, defaultValue)
        }
    } ?: propertyDefaultIfUnset(propertyName, defaultValue)
}
