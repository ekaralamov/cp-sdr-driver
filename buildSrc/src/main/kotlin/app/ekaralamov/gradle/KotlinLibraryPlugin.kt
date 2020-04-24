package app.ekaralamov.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class KotlinLibraryPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        apply(plugin = "com.android.library")
        apply(plugin = "app.ekaralamov.kotlin")

        dependencies {
            "testImplementation"(project(":kotest"))
        }
    }
}
