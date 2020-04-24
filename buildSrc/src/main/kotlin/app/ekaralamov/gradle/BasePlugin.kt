package app.ekaralamov.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get

class BasePlugin : Plugin<Project> {

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
internal fun <T> Project.versionOf(dependency: String) =
    (rootProject.extensions["Versions"] as ExtraPropertiesExtension)[dependency] as T
