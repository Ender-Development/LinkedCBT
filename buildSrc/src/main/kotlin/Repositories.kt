import org.gradle.api.Project
import org.gradle.kotlin.dsl.repositories

fun Project.loadDefaultRepositories() {
    repositories {
        mavenCentral()
        // RetroFuturaGradle
        maven {
            name = "GTNH Maven"
            url = uri("https://nexus.gtnewhorizons.com/repository/public/")
        }
        // JitPack is a novel package repository for JVM and Android projects.
        // It builds Git projects on demand and provides you with ready-to-use artifacts (jar, aar).
        // Docs: https://docs.jitpack.io/
        maven {
            name = "JitPack"
            url = uri("https://jitpack.io")
        }
        maven {
            name = "SpongePowered Maven"
            url = uri("https://repo.spongepowered.org/maven")
        }
        // HEI, MixinBooter, GroovyScript, Forgelin Continuous, ...
        maven {
            name = "CleanroomMC Maven"
            url = uri("https://maven.cleanroommc.com")
        }
        // Our own maven, meow :3
        maven {
            name = "Ender-Development Maven"
            url = uri("https://maven.ender-development.org/")
        }
        // Better implementation of the curseforge maven, which allows adding mods that disable third party downloads
        exclusiveContent {
            forRepository {
                maven {
                    name = "CurseMaven"
                    url = uri("https://curse.cleanroommc.com")
                }
            }
            filter {
                includeGroup("curse.maven")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    name = "Modrinth"
                    url = uri("https://api.modrinth.com/maven")
                }
            }
            filter {
                includeGroup("maven.modrinth")
            }
        }
        // CraftTweaker, ContentTweaker, BWM, JEI, ...
        maven {
            name = "BlameJared's Maven"
            url = uri("https://maven.blamejared.com/")
        }
        // GTCE, GTCEu, AE2uel, EnderIO
        maven {
            name = "GTCEu Maven"
            url = uri("https://maven.gtceu.com")
        }
        // AE2, Mekanism, ProjectE, ComputerCraft, OpenComputers, ...
        maven {
            name = "Thiakil's Maven"
            url = uri("https://maven.thiakil.com/")
        }
        maven {
            name = "AppleCore's Maven"
            url = uri("https://www.ryanliptak.com/maven/")
        }
        // TiCo, Mantle, HungerOverhaul, Natura, IronChest, ...
        maven {
            name = "Mantle's Maven"
            url = uri("https://dvs1.progwml6.com/files/maven")
        }
        // CTM
        maven {
            name = "tterrag's Maven"
            url = uri("https://maven.tterrag.com/")
        }
        // cofh, codechicken, ProjectRed, p455w0rd, brandon3055, ...
        maven {
            name = "covers1624's Maven"
            url = uri("https://maven.covers1624.net/")
        }
        maven {
            name = "modmuss50's Maven"
            url = uri("https://maven.modmuss50.me/")
        }
        maven {
            name = "BuildCraft's Maven"
            url = uri("https://mod-buildcraft.com/maven/")
        }
        // Large collection of mods from various authors, curseforge maven precursor
        maven {
            name = "ModMaven"
            url = uri("https://modmaven.dev/")
        }
        mavenLocal() // Must be last for caching to work
    }
}
