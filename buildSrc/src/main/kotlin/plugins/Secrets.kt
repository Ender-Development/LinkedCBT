package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class Secrets : Plugin<Project> {
    companion object {
        const val PROPERTIES_FILE = "secrets.properties"
        const val EXAMPLE_FILE = "secrets.example.properties"
        private var secretsFile = ""

        /**
         * Loads properties from the specified properties file located in the resources' directory.
         *
         * @param name The name of the property to load from the secrets.properties file.
         */
        operator fun get(name: String) = Loader.getPropertyFromFile(secretsFile, name)

        /**
         * Loads properties from the specified properties file located in the resources' directory.
         * If the property is not found, it attempts to load it from the system environment variables.
         *
         * @param name The name of the property to load from the secrets.properties file or environment variables.
         * @return The value of the property or environment variable, or null if not found.
         */
        fun getOrEnvironment(name: String): String? = get(name) ?: System.getenv(name)
    }

    override fun apply(target: Project) {
        Logger.greet(this)

        val rootSecrets = target.rootProject.file(PROPERTIES_FILE)
        val userSecrets = File(
            System.getProperty("user.home"),
            ".gradle/$PROPERTIES_FILE",
        )

        val secretsFile = when {
            rootSecrets.exists() -> {
                if (System.getenv("DISMISS_SECRET_FILE_WARNING") == null) {
                    Logger.warn(
                        "Keeping the secrets file in the project root can have security implications, as such moving the '$PROPERTIES_FILE' to ~/.gradle would be recommended. Set the DISMISS_SECRET_FILE_WARNING environment variable to disable this warning at your own risk.",
                    )
                }
                rootSecrets
            }

            userSecrets.exists() -> userSecrets

            else -> {
                if (target.rootProject.file(EXAMPLE_FILE).exists()) {
                    Logger.warn("No '$PROPERTIES_FILE' found, please create one in ~/.gradle, or in the project root, based on '$EXAMPLE_FILE'.")
                }

                return
            }
        }

        Companion.secretsFile = secretsFile.absolutePath
        Logger.info("Loading secrets from: ${secretsFile.absolutePath}")
        Loader.loadPropertyFile(secretsFile.absolutePath)
    }
}
