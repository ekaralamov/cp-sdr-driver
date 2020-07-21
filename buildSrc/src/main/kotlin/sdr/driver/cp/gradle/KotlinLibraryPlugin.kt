package sdr.driver.cp.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
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

        configure<BaseExtension> {
            packagingOptions {
                exclude("META-INF/*.kotlin_module")
            }
        }
    }
}
