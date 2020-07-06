package sdr.driver.cp.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class KotlinLibraryPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        apply(plugin = "com.android.library")
        apply(plugin = "sdr.driver.cp.kotlin")

        tasks.withType<Test> {
            useJUnitPlatform()
        }

        dependencies {
            "testImplementation"(project(":kotest"))
        }
    }
}
