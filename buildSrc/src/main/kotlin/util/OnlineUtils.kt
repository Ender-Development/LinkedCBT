package util

import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import plugins.Logger
import plugins.Secrets
import java.io.File
import java.net.SocketTimeoutException
import java.net.URI
import java.net.UnknownHostException

object OnlineUtils {
    const val TEMPLATE_REPO = "Ender-Development/Catalyx-Template"
    const val TEMPLATE_BRANCH = "master"
    const val GITHUB_RAW_URL = "https://raw.githubusercontent.com"
    const val CONNECTION_TIMEOUT = 5000 // 5 seconds

    /**
     * Determines whether synchronization with the template project should be disabled.
     * Checks if the current project is the template project itself or if the
     * `SYNC_TEMPLATE` environment variable is set to `false`.
     * @return `true` if synchronization should be disabled, `false` otherwise.
     */
    fun shouldDisableSync(): Boolean {
        if (isTemplateProject()) {
            Logger.info("Current project is the template project, skipping sync.")
            return true
        }
        if (Secrets.getOrEnvironment("SYNC_TEMPLATE")?.toBoolean() == false) {
            Logger.info("SYNC_TEMPLATE is set to false, skipping sync.")
            return true
        }
        return false
    }

    /**
     * Checks if the current project is the template project by examining the Git remote URL.
     * @return `true` if the project is the template project, `false` otherwise.
     */
    fun isTemplateProject(): Boolean {
        val repo = FileRepositoryBuilder()
            .setGitDir(File(".git"))
            .readEnvironment()
            .findGitDir()
            .build()
        val remoteUrl = repo.config.getString("remote", "origin", "url")
        Logger.info("Remote URL detected: $remoteUrl")
        return remoteUrl.contains(TEMPLATE_REPO)
    }

    /**
     * Checks if there is an active internet connection by attempting to connect to GitHub's API.
     * @return `true` if an internet connection is detected, `false` otherwise.
     */
    fun isOnline() = try {
        val connection = URI.create(GITHUB_RAW_URL).toURL().openConnection()
        connection.connectTimeout = CONNECTION_TIMEOUT
        connection.readTimeout = CONNECTION_TIMEOUT
        connection.connect()
        connection.inputStream.close()
        Logger.info("Internet connection detected.")
        true
    } catch (e: UnknownHostException) {
        Logger.error("No internet connection: ${e.message}")
        false
    } catch (e: SocketTimeoutException) {
        Logger.error("Connection timed out: ${e.message}")
        false
    } catch (e: Exception) {
        Logger.error("Error checking internet connection: ${e.message}")
        false
    }

    /**
     * Fetches the content of a file from the specified URL.
     * @param url The URL of the file to fetch.
     * @return The content of the file as a String, or null if an error occurs.
     */
    fun fetchFileContent(url: String): String? = try {
        val connection = URI.create(url).toURL().openConnection()
        connection.connectTimeout = CONNECTION_TIMEOUT
        connection.readTimeout = CONNECTION_TIMEOUT
        connection.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
    } catch (e: Exception) {
        Logger.error("Error fetching file from '$url': ${e.message}")
        null
    }
}
