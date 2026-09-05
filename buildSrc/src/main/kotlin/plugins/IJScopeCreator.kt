package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import propertyString
import util.OnlineUtils
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.div
import kotlin.io.path.notExists
import kotlin.io.path.writeText

@Suppress("UNUSED")
class IJScopeCreator : Plugin<Project> {
    companion object {
        const val SCOPE_FILE = "Mod_Source.xml"

        private fun createIJScope(project: Project) {
            if (OnlineUtils.isTemplateProject()) {
                return
            }

            val modName = project.propertyString("mod_name")
            val filteredModName = modName.filter(Char::isLetterOrDigit)
            val outputPath = project.rootDir.toPath() / ".idea" / "scopes" / SCOPE_FILE

            val ijScope = buildString {
                appendLine("<component name=\"DependencyValidationManager\">")
                appendLine("  <scope name=\"$modName Source\" pattern=\"file[$filteredModName.main]:*/&amp;&amp;!file[$filteredModName.main]:resources//*\" />")
                appendLine("</component>")
            }

            if (outputPath.notExists()) {
                outputPath.parent.createDirectories()
                outputPath.createFile()
            }
            outputPath.writeText(ijScope)
            Logger.info("IntelliJ scope file created at ${outputPath.normalize()}")
        }
    }

    override fun apply(target: Project) {
        Logger.greet(this)
        createIJScope(target)
    }
}
