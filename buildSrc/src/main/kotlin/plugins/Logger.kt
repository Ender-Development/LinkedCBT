package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

class Logger : Plugin<Project> {
    enum class Level {
        INFO,
        WARN,
        ERROR,
    }

    companion object {
        const val LOG_PATH = "gradle/catalyx.log"

        private lateinit var logFile: File

        private fun log(message: String, level: Level = Level.INFO) {
            val timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            PrintWriter(FileWriter(logFile, true)).use { out ->
                out.println("[$timestamp] [${level.name}] $message")
            }
        }

        fun info(message: String) = log(message, Level.INFO)
        fun warn(message: String) = log(message, Level.WARN)
        fun error(message: String) = log(message, Level.ERROR)

        fun greet(plugin: Plugin<Project>) {
            banner("Applying ${plugin::class.simpleName} plugin!")
        }

        fun banner(message: String) {
            info("=".repeat(message.length + 4))
            info("= $message =")
            info("=".repeat(message.length + 4))
        }
    }

    override fun apply(target: Project) {
        logFile = target.rootProject.file(LOG_PATH)
        if (logFile.exists()) {
            logFile.delete()
        }
        greet(this)
        info("Logger initialized. Logging to ${logFile.absolutePath}")
    }
}
