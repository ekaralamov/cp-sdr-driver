package app.ekaralamov.gradle

import com.android.build.gradle.TestExtension
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class TestPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        apply(plugin = "com.android.test")
        apply(plugin = "app.ekaralamov.kotlin")

        configure<TestExtension> {
            defaultConfig {
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            packagingOptions {
                exclude("META-INF/*.kotlin_module")
            }

            applicationVariants.all {
                val variant: ApplicationVariant = this
                val configuration = configurations.create("${variant.name}TargetJavaDependencies") {
                    isCanBeResolved = true
                    isCanBeConsumed = false
                }
                dependencies {
                    configuration(
                        project(
                            "path" to targetProjectPath,
                            "configuration" to ExposeJavaDependencies.configurationName(variant.name)
                        )
                    )
                }
                configuration.incoming.files.singleFile.forEachLine { identifier ->
                    variant.runtimeConfiguration.resolutionStrategy.force(identifier)
                    variant.compileConfiguration.resolutionStrategy.force(identifier)
                }
            }
        }
    }
}
