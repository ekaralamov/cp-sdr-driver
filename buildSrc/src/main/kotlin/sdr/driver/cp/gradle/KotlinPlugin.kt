package sdr.driver.cp.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KotlinPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        apply(plugin = "kotlin-android")
        apply(plugin = "kotlin-android-extensions")
        apply(plugin = "sdr.driver.cp.base")

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
                    version = "1.3.2"
                )
                invoke(
                    group = "org.jetbrains.kotlinx",
                    name = "kotlinx-coroutines-core",
                    version = "1.4.0"
                )
            }
        }

        tasks.withType(KotlinCompile::class.java).configureEach {
            kotlinOptions.freeCompilerArgs += arrayOf(
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xopt-in=kotlin.time.ExperimentalTime"
            )
        }
    }
}
