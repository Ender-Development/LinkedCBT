package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import util.OnlineUtils

class ScriptSync : Plugin<Project> {
    companion object {
        const val BASE_URL = "${OnlineUtils.GITHUB_RAW_URL}/${OnlineUtils.TEMPLATE_REPO}/${OnlineUtils.TEMPLATE_BRANCH}/"
        const val FILE_LIST_NAME = "buildSrc/src/main/resources/sync-file-list.txt"
        private lateinit var project: Project

        fun syncFilesFromTemplate() {
            Logger.banner("Searching for Files to sync!")
            if (OnlineUtils.shouldDisableSync()) return Logger.info("Sync is disabled via system.")
            if (!OnlineUtils.isOnline()) return Logger.warn("No internet connection detected.")
            performSync()
        }

        private fun syncFile(fileName: String) {
            val fileUrl = "$BASE_URL$fileName"
            val remoteContent = OnlineUtils.fetchFileContent(fileUrl) ?: throw Exception("Failed to fetch content from $fileUrl")
            val localFile = project.file(fileName)
            val localContent = if (localFile.exists()) localFile.readText() else ""

            if (remoteContent != localContent) {
                localFile.writeText(remoteContent)
                Logger.info("Synchronized file: $fileName")
            } else {
                Logger.info("File is up-to-date: $fileName")
            }
        }

        private fun performSync() {
            syncFile(FILE_LIST_NAME)

            project.file(FILE_LIST_NAME).useLines { lines ->
                lines.filter(String::isNotBlank).forEach(::syncFile)
            }
        }
    }

    override fun apply(target: Project) {
        project = target
        Logger.greet(this)
    }
}
