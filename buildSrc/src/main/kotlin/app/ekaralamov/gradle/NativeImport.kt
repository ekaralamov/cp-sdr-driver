package app.ekaralamov.gradle

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import java.nio.file.Files
import java.nio.file.Path

class NativeImport : Plugin<Project> {

    override fun apply(project: Project) = with(project) {

        fun apply(variant: LibraryVariant, variantLinksRoot: Path) {
            val rootTask = tasks.create("${variant.name}PrepareNativeImportsLinksRoot", Delete::class) {
                delete(variantLinksRoot)
                doLast {
                    Files.createDirectories(variantLinksRoot)
                }
            }

            fun createLinksTask(subject: String, configuration: Configuration, artifactType: String) =
                tasks.create("${variant.name}Create${subject}ImportsLinks") {
                    dependsOn(rootTask)
                    inputs.files(configuration.incoming.artifactView {
                        attributes.attribute(ArtifactTypeAttribute, artifactType)
                    }.files)
                    doLast {
                        val subjectLinksDir = variantLinksRoot.resolve(subject)
                        Files.createDirectory(subjectLinksDir)
                        var n = 0
                        inputs.files.forEach {
                            Files.createSymbolicLink(
                                subjectLinksDir.resolve((++n).toString()),
                                it.toPath()
                            )
                        }
                    }
                }

            val cppHeadersTask = createLinksTask(
                "CppHeaders",
                variant.compileConfiguration,
                NativeExport.ArtifactType.CppHeaderDir
            )
            val runtimeTask = createLinksTask(
                "Runtime",
                variant.runtimeConfiguration,
                "android-jni"
            )
            val libNamesTask = createLinksTask(
                "LibNames",
                variant.runtimeConfiguration,
                NativeExport.ArtifactType.LibNames
            )

            variant.preBuildProvider.configure {
                dependsOn(
                    cppHeadersTask,
                    runtimeTask,
                    libNamesTask
                )
            }
        }

        configure<LibraryExtension> {
            val linksRoot = buildDir.toPath().resolve("nativeImportsLinks")
            libraryVariants.all {
                val variant: LibraryVariant = this
                apply(variant, linksRoot.resolve(variant.name))
            }
            defaultConfig.externalNativeBuild.cmake.arguments(
                "-DMY_NATIVE_IMPORTS_ROOT=$linksRoot",
                "-DMY_NATIVE_IMPORTS_SCRIPT=${rootDir}/buildSrc/NativeImport.CMake"
            )
        }
    }

    companion object {

        private val ArtifactTypeAttribute = Attribute.of("artifactType", String::class.java)
    }
}
