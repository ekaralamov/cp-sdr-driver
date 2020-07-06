package sdr.driver.cp.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import java.io.File

class ExposeJavaDependencies : Plugin<Project> {

    override fun apply(project: Project) = project.afterEvaluate {
        configure<AppExtension> {
            applicationVariants.all {
                val variant: ApplicationVariant = this
                val exposedJavaDependenciesFile =
                    File(buildDir, "exposedJavaDependencies/${variant.name}")

                exposedJavaDependenciesFile.ensureParentDirsCreated()
                exposedJavaDependenciesFile.createNewFile()
                exposedJavaDependenciesFile.printWriter().use { writer ->
                    variant.getCompileClasspathArtifacts(null).forEach { artifact ->
                        val identifier = artifact.id.componentIdentifier.toString()
                        if ("[^ :]+:[^ :]+:[^ :]+".toRegex() matches identifier)
                            writer.println(identifier)
                    }
                }

                configurations.create(
                    configurationName(
                        variant.name
                    )
                ) {
                    isCanBeConsumed = true
                    isCanBeResolved = false
                    outgoing.artifact(exposedJavaDependenciesFile)
                }
            }
        }
    }

    companion object {
        internal fun configurationName(variantName: String) =
            "${variantName}ExposedJavaDependencies"
    }
}
