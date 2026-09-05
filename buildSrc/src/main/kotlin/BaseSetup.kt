import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.buildscript
import org.gradle.kotlin.dsl.repositories

fun Project.loadDefaultSetup() {
    buildscript {
        repositories {
            mavenCentral()
            google()
        }
        dependencies {
            classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${propertyString("kotlin_version")}")
        }
    }

    val embed = configurations.create("embed")
    configurations.getByName("implementation").extendsFrom(embed)
    configurations.all { resolutionStrategy.cacheChangingModulesFor(0, "seconds") }

    group = propertyString("root_package")
    version = propertyString("mod_version")

    extensions.configure(JavaPluginExtension::class.java) {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
            // Azul covers the most platforms for Java 8+ toolchains, crucially including macOS arm64
            vendor.set(JvmVendorSpec.AZUL)
        }
        if (propertyBoolean("generate_sources_jar")) withSourcesJar()
        if (propertyBoolean("generate_javadocs_jar")) withJavadocJar()
    }

    extensions.configure(BasePluginExtension::class.java) {
        archivesName.set(propertyString("mod_id"))
    }
}
