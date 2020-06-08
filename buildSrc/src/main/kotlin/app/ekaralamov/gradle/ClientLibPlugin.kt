package app.ekaralamov.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get

class ClientLibPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        apply(plugin = "com.android.library")
        apply(plugin = "kotlin-android")
        apply(plugin = "app.ekaralamov.base")

        dependencies {
            with(configurations["implementation"]) {
                invoke(
                    group = "org.jetbrains.kotlin",
                    name = "kotlin-stdlib",
                    version = versionOf("Kotlin")
                )
            }
        }
    }
}
