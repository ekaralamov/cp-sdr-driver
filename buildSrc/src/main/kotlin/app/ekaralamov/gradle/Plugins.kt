package app.ekaralamov.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get

class BaseApplicationPlugin : BaseKotlinPlugin() {
    override fun apply(project: Project) = with(project) {
        apply(plugin = "com.android.application")
        super.apply(project)
    }
}

open class BaseTestPlugin : BaseKotlinPlugin() {
    override fun apply(project: Project) = with(project) {
        apply(plugin = "com.android.test")
        super.apply(project)

        configure<BaseExtension> {
            defaultConfig {
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
        }
    }
}

open class BaseKotlinPlugin : BasePlugin() {
    override fun apply(project: Project) = with(project) {
        apply(plugin = "kotlin-android")
        apply(plugin = "kotlin-android-extensions")
        super.apply(project)

        configure<BaseExtension> {
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
        }

        dependencies {
            with(configurations["implementation"]) {
                invoke(
                    group = "org.jetbrains.kotlin",
                    name = "kotlin-stdlib-jdk8",
                    version = versionOf("Kotlin")
                )
                invoke(
                    group = "androidx.core",
                    name = "core-ktx",
                    version = "1.2.0"
                )
            }
        }
    }
}

open class BaseLibraryPlugin : BasePlugin() {
    override fun apply(project: Project) = with(project) {
        apply(plugin = "com.android.library")
        super.apply(project)
    }
}

open class BasePlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        configure<BaseExtension> {
            setCompileSdkVersion(versionOf<Int>("CompileSdk"))

            defaultConfig {
                setMinSdkVersion(versionOf<Int>("MinSdk"))
                setTargetSdkVersion(versionOf<Int>("CompileSdk"))
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
internal fun <T> Project.versionOf(dependency: String) = (rootProject.extensions["Versions"] as ExtraPropertiesExtension)[dependency] as T
