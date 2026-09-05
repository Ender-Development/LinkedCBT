import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.compiler
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers
import plugins.DepLoader
import plugins.Logger
import plugins.PropSync
import plugins.ScriptSync
import plugins.Secrets
import util.EnumConfiguration
import java.nio.file.Files
import java.nio.file.StandardCopyOption

loadDefaultSetup()

plugins {
    id("catalyx.logger")
    id("catalyx.loader")
    id("catalyx.secrets")
    id("catalyx.deploader")
    id("catalyx.propsync")
    id("catalyx.buildfilesync")
    id("catalyx.referencecreator") apply false
    id("catalyx.ijscopecreator")
    id("java")
    id("java-library")
    id("maven-publish")
    kotlin("jvm") version libs.versions.kotlinVersion
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.9"
    id("com.gtnewhorizons.retrofuturagradle") version "1.4.9"
    id("org.jetbrains.changelog") version "2.2.1"
    // Publishing
    id("com.matthewprenger.cursegradle") version "1.4.0" apply false
    id("com.modrinth.minotaur") version "2.+" apply false
    // Formatters
    id("com.diffplug.spotless") version "8.0.0" apply false
}

checkPropertyExists("root_package")
checkPropertyExists("mod_id")
propertyDefaultIfUnset("mod_name", propertyString("mod_id"))
checkPropertyExists("mod_version")
checkPropertyExists("minecraft_version")
propertyDefaultIfUnsetWithEnvVar("minecraft_username", "DEV_USERNAME", "Developer")

// Utilities
checkSubPropertiesExist("use_tags", "tags_package")
checkSubPropertiesExist("use_access_transformer", "access_transformer_locations")
checkSubPropertiesExist("is_coremod", "coremod_includes_mod", "coremod_plugin_class_name")

// Dependencies
checkSubPropertiesExist("use_assetmover", "assetmover_version")
checkSubPropertiesExist("use_catalyx", "catalyx_version")
checkSubPropertiesExist("use_configanytime", "configanytime_version")
checkSubPropertiesExist("use_forgelincontinuous", "forgelin_continuous_version")
checkSubPropertiesExist("use_mixinbooter", "mixin_booter_version", "mixin_refmap")
checkSubPropertiesExist("use_modularui", "modularui_version")

// Integrations
checkSubPropertiesExist("use_crafttweaker", "crafttweaker_version")
checkSubPropertiesExist("use_groovyscript", "groovyscript_version")
checkSubPropertiesExist("use_hei", "hei_version")
checkSubPropertiesExist("use_top", "top_version")

kotlin {
    jvmToolchain(8)
    compilerOptions {
        extraWarnings.set(true)
    }
}

minecraft {
    mcVersion = propertyString("minecraft_version")
    mcpMappingChannel = propertyString("mapping_channel")
    mcpMappingVersion = propertyString("mapping_version")

    username = propertyString("minecraft_username")

    useDependencyAccessTransformers = propertyBoolean("use_dependency_at_files")

    extraRunJvmArguments.add("-ea:$group")
    val args = mutableListOf<String>()
    if (propertyBoolean("use_mixinbooter")) {
        args.add("-Dmixin.hotSwap=true")
        args.add("-Dmixin.checks.interfaces=true")
        args.add("-Dmixin.debug.export=true")
    }
    if (propertyBoolean("is_coremod")) {
        args.add("-Dlegacy.debugClassLoading=true")
        args.add("-Dlegacy.debugClassLoadingFiner=true")
        args.add("-Dlegacy.debugClassLoadingSave=true")
    }
    extraRunJvmArguments.addAll(args)
    extraRunJvmArguments.addAll(propertyStringList("extra_jvm_args", delimiter = ";"))

    if (propertyBoolean("use_tags")) {
        apply(plugin = "catalyx.referencecreator")
    }
}

Logger.banner("Configuring Repositories")
loadDefaultRepositories()

Logger.banner("Configuring Dependencies")
loadDefaultDependencies()

// These are only here as I can't get RetroFuturaGradle to work in our buildSrc
dependencies {
    // Mixins
    if (propertyBoolean("use_mixinbooter") || propertyBoolean("use_modularui")) {
        val mixinBooter = "zone.rong:mixinbooter:${propertyString("mixin_booter_version")}"
        val mixinRefMap = propertyString("mixin_refmap")
        val mixin = modUtils.enableMixins(mixinBooter, mixinRefMap).toString()
        api(mixin) { isTransitive = false }
        annotationProcessor("org.ow2.asm:asm-debug-all:5.2")
        annotationProcessor("com.google.guava:guava:32.1.2-jre")
        annotationProcessor("com.google.code.gson:gson:2.8.9")
        annotationProcessor(mixin) { isTransitive = false }
    }

    // TOP
    val top = "curse.maven:theonesmeagle-977883:${propertyString("top_version")}"
    if (propertyBoolean("use_top")) dep(EnumConfiguration.IMPLEMENTATION, top) else dep(EnumConfiguration.COMPILE_ONLY, (rfg.deobf(top)))

    // Additional dependencies
    DepLoader.get().forEach {
        val runtimeOnly = it.configuration == EnumConfiguration.RUNTIME_ONLY
        if (!it.enabled && runtimeOnly) {
            // Skip disabled runtime-only dependencies
            Logger.info("Skipping disabled runtime-only dependency: ${it.source}")
            return@forEach
        }
        dep(
            it.configuration,
            // Use deobfuscated version for compile-time if not runtime-only
            if (runtimeOnly) it.source else (rfg.deobf(it.source)),
            it.transitive,
            it.changing,
        )
    }
}

// Manage Access Transformers
if (propertyBoolean("use_access_transformer")) {
    propertyStringList("access_transformer_locations").forEach {
        val atFile = file("$projectDir/src/main/resources/$it")
        if (atFile.exists()) {
            tasks.deobfuscateMergedJarToSrg.get().accessTransformerFiles.from(atFile)
            tasks.srgifyBinpatchedJar.get().accessTransformerFiles.from(atFile)
        } else {
            throw GradleException("Access Transformer file '$it' does not exist!")
        }
    }
}

// Spotless for code formatting
if (propertyBoolean("use_spotless")) {
    apply(plugin = "com.diffplug.spotless")
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        encoding("UTF-8")

        format("misc") {
            target(".gitignore", "*.properties", "buildSrc/src/**/*.properties")
            trimTrailingWhitespace()
            endWithNewline()
        }

        if (propertyString("editorconfig") == "spotless") {
            kotlin {
                target("src/*/kotlin/**/*.kt", "buildSrc/src/**/*.kt")
                ktlint(propertyString("ktlint_version"))
            }

            kotlinGradle {
                target("*.gradle.kts", "buildSrc/src/**/*.gradle.kts")
                ktlint(propertyString("ktlint_version"))
            }

            java {
                target("src/*/java/**/*.java")
                removeUnusedImports()
                forbidWildcardImports()
                googleJavaFormat(propertyString("google_java_format_version"))
                formatAnnotations()
            }

            json {
                target("src/*/resources/**/*.json")
                gson().indentWithSpaces(2)
            }
        }

        freshmark {
            target(".github/**/*.md", "docs/**/*.md", "src/*/resources/**/*.md", "*.md")
            propertiesFile("gradle.properties")
        }

        flexmark {
            target(".github/**/*.md", "docs/**/*.md", "src/*/resources/**/*.md")
            flexmark(propertyString("flexmark_version"))
        }
    }
}

tasks.withType<ProcessResources> {
    // This will ensure that this task is redone when the versions change
    inputs.property("minecraft_version", propertyString("minecraft_version"))
    inputs.property("mod_id", propertyString("mod_id"))
    inputs.property("mod_name", propertyString("mod_name"))
    inputs.property("mod_version", propertyString("mod_version"))
    inputs.property("mod_description", propertyString("mod_description"))
    inputs.property("mod_authors", propertyStringList("mod_authors", ",").joinToString(", "))
    inputs.property("mod_credits", propertyString("mod_credits"))
    inputs.property("mod_url", propertyString("mod_url"))
    inputs.property("mod_logo_path", propertyString("mod_logo_path"))
    inputs.property("mixin_refmap", propertyString("mixin_refmap"))

    // Replace various properties in mcmod.info and pack.mcmeta if applicable
    filesMatching(arrayListOf("mcmod.info", "pack.mcmeta")) {
        expand(
            "minecraft_version" to propertyString("minecraft_version"),
            "mod_id" to propertyString("mod_id"),
            "mod_name" to propertyString("mod_name"),
            "mod_version" to propertyString("mod_version"),
            "mod_description" to propertyString("mod_description"),
            "mod_authors" to propertyStringList("mod_authors", ",").joinToString(", "),
            "mod_credits" to propertyString("mod_credits"),
            "mod_url" to propertyString("mod_url"),
            "mod_logo_path" to propertyString("mod_logo_path"),
            "mixin_refmap" to propertyString("mixin_refmap"),
        )
    }

    if (propertyBoolean("use_access_transformer")) {
        // Make sure Access Transformer files are in META-INF folder
        rename("(.+_at.cfg)", "META-INF/$1")
    }
}

tasks.withType<Javadoc> {
    exclude("**/package-info.java")
}

tasks.withType<Jar> {
    manifest {
        val attributeMap = mutableMapOf<String, String>()
        if (propertyBoolean("is_coremod")) {
            attributeMap["FMLCorePlugin"] = propertyString("coremod_plugin_class_name")
            if (propertyBoolean("coremod_includes_mod")) {
                attributeMap["FMLCorePluginContainsFMLMod"] = "true"
                val currentTask = gradle.startParameter.taskNames
                val validTasks = arrayOf("build", "prepareObfModsFolder", "runObfClient")
                if (!currentTask.isEmpty() && currentTask[0] in validTasks) attributeMap["ForceLoadAsMod"] = "true"
            }
        }
        if (propertyBoolean("use_access_transformer")) {
            attributeMap["FMLAT"] = propertyString("access_transformer_locations")
        }
        if (propertyBoolean("use_mixinbooter")) {
            attributeMap["MixinConnector"] = propertyString("mixin_connector_class")
        }
        attributes(attributeMap)
    }
    // Add all embedded dependencies into the jar
    from(provider { configurations.getByName("embed").map { if (it.isDirectory()) it else zipTree(it) } })
}

tasks.withType<JavaCompile> { options.encoding = "UTF-8" }

tasks.register("catalyxAfterSync") {
    group = "catalyx"
    description = "Task that runs after the template has been synced. Can be used for custom actions."
    dependsOn("switchEditorConfig", "catalyxReference")
}

tasks.register("catalyxReference") {
    group = "catalyx"
    description = "Generates the Reference.kt file from tags.properties."
    doLast {
        if (propertyBoolean("use_tags")) {
            apply(plugin = "catalyx.referencecreator")
        } else {
            Logger.warn("Property 'use_tags' is false; skipping Reference.kt generation.")
        }
    }
}

tasks.named("prepareObfModsFolder").configure { finalizedBy("prioritizeCoremods") }

tasks.named("processIdeaSettings").configure { dependsOn("catalyxReference") }

tasks.register("prioritizeCoremods") {
    dependsOn("prepareObfModsFolder")
    doLast {
        fileTree("run/obfuscated").forEach {
            if (it.isFile && it.name.matches(Regex("(mixinbooter|configanytime)-[0-9]+\\.[0-9]+\\.jar"))) {
                it.renameTo(File(it.parentFile, "!${it.name}"))
            }
        }
    }
}

tasks.register("syncTemplate") {
    group = "catalyx"
    description = "Syncs the project properties and buildscript files with the remote template properties."
    doLast {
        PropSync.syncPropertiesFromTemplate()
        ScriptSync.syncFilesFromTemplate()
    }
}

val runTasks = listOf("runClient", "runServer", "runObfClient", "runObfServer")
runTasks.forEach {
    tasks.named<JavaExec>(it).configure {
        if (propertyBoolean("is_coremod")) {
            jvmArgs("-Dfml.coreMods.load=${propertyString("coremod_plugin_class_name")}")
        }
        if (it.contains("Client")) {
            val groovyOptions = mapOf(
                "grs_use_examples_folder" to "-Dgroovyscript.use_examples_folder=true",
                "grs_run_ls" to "-Dgroovyscript.run_ls=true",
                "grs_generate_examples" to "-Dgroovyscript.generate_examples=true",
                "grs_generate_wiki" to "-Dgroovyscript.generate_wiki=true",
                "grs_generate_and_crash" to "-Dgroovyscript.generate_and_crash=true",
                "grs_log_missing_lang_keys" to "-Dgroovyscript.log_missing_lang_keys=true",
            )
            groovyOptions.forEach { (prop, arg) ->
                if (propertyBoolean(prop)) {
                    Logger.info("Adding GroovyScript option '$arg' to $it")
                    jvmArgs(arg)
                }
            }
        }
    }
}

tasks.register("switchEditorConfig") {
    group = "catalyx"
    description = "Switches the .editorconfig file based on the 'editorconfig' property."
    val spotlessEditorConfig = file("$projectDir/buildSrc/src/main/resources/.editorconfig.spotless")
    val rozEditorConfig = file("$projectDir/buildSrc/src/main/resources/.editorconfig.roz")

    val destination = file("$projectDir/.editorconfig")

    doFirst {
        val sourceFile = when (propertyString("editorconfig")) {
            "spotless" -> spotlessEditorConfig
            "roz" -> rozEditorConfig
            else -> throw IllegalArgumentException("Unknown editorConfigType: ${propertyString("editorconfig")}")
        }
        if (!sourceFile.exists()) {
            throw GradleException("Source .editorconfig file '${sourceFile.path}' does not exist!")
        }
        Logger.info("Switching .editorconfig to use '${sourceFile.name}'")
        Files.copy(sourceFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}

Logger.banner("Configuring IDEA")
idea {
    module {
        inheritOutputDirs = true
        isDownloadJavadoc = true
        isDownloadSources = true
    }
    project {
        settings {
            taskTriggers { afterSync("catalyxAfterSync") }
            runConfigurations {
                var index = 0
                add(Gradle("${index++}. Setup Workspace").apply { setProperty("taskNames", listOf("setupDecompWorkspace")) })
                add(Gradle("${index++}. Run Client").apply { setProperty("taskNames", listOf("runClient")) })
                add(Gradle("${index++}. Run Server").apply { setProperty("taskNames", listOf("runServer")) })
                add(Gradle("${index++}. Run Obfuscated Client").apply { setProperty("taskNames", listOf("runObfClient")) })
                add(Gradle("${index++}. Run Obfuscated Server").apply { setProperty("taskNames", listOf("runObfServer")) })
                if (propertyBoolean("use_spotless")) {
                    add(Gradle("${index++}. Apply Spotless").apply { setProperty("taskNames", listOf("spotlessApply")) })
                }
                add(Gradle("${index++}. Build Jars").apply { setProperty("taskNames", listOf("build")) })
                if (propertyBoolean("publish_to_maven")) {
                    add(Gradle("${index++}. Publish to Maven").apply { setProperty("taskNames", listOf("publish")) })
                }
                if (Secrets.getOrEnvironment("SYNC_TEMPLATE")?.toBoolean() != false) {
                    add(Gradle("${index++}. Sync Template").apply { setProperty("taskNames", listOf("syncTemplate")) })
                }
                Logger.info("Added $index run configurations to the IDE")
            }
            compiler.javac {
                afterEvaluate {
                    javacAdditionalOptions = "-encoding utf8"
                    moduleJavacAdditionalOptions =
                        mapOf("${project.name}.main" to tasks.compileJava.get().options.compilerArgs.joinToString(" ") { "\"$it\"" })
                }
            }
        }
    }
}

if (propertyBoolean("publish_to_maven")) {
    publishing {
        checkPropertyExists("maven_name")
        checkPropertyExists("maven_url")
        repositories {
            maven {
                name = propertyString("maven_name")
                url = uri(propertyString("maven_url"))
                credentials {
                    username = Secrets.getOrEnvironment("MAVEN_USERNAME")
                    password = Secrets.getOrEnvironment("MAVEN_PASSWORD")
                }
            }
        }
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                groupId = propertyString("root_package")
                artifactId = propertyString("mod_id")
            }
        }
    }
}
