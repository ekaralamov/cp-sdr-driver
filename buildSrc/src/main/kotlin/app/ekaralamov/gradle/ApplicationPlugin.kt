package app.ekaralamov.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class ApplicationPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        apply(plugin = "com.android.application")
        apply(plugin = "app.ekaralamov.kotlin")
    }
}
