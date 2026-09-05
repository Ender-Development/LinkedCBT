plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.4.0.202509020913-r")
}

gradlePlugin {
    plugins {
        create("secretsPlugin") {
            id = "catalyx.secrets"
            implementationClass = "plugins.Secrets"
        }
        create("loggerPlugin") {
            id = "catalyx.logger"
            implementationClass = "plugins.Logger"
        }
        create("loaderPlugin") {
            id = "catalyx.loader"
            implementationClass = "plugins.Loader"
        }
        create("depLoaderPlugin") {
            id = "catalyx.deploader"
            implementationClass = "plugins.DepLoader"
        }
        create("propSyncPlugin") {
            id = "catalyx.propsync"
            implementationClass = "plugins.PropSync"
        }
        create("buildFileSync") {
            id = "catalyx.buildfilesync"
            implementationClass = "plugins.ScriptSync"
        }
        create("referenceCreatorPlugin") {
            id = "catalyx.referencecreator"
            implementationClass = "plugins.ReferenceCreator"
        }
        create("ijScopeCreatorPlugin") {
            id = "catalyx.ijscopecreator"
            implementationClass = "plugins.IJScopeCreator"
        }
    }
}
