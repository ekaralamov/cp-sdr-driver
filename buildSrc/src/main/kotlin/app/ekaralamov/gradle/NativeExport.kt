package app.ekaralamov.gradle

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import java.io.File

open class NativeExportExtension {
    lateinit var cppHeaderDirs: List<File>
}

class NativeExport : Plugin<Project> {

    object ArtifactType {

        val CppHeaderDir = "cpp-header-dir"
        val LibNames = "lib-names"
    }

    override fun apply(project: Project) = with(project) {
        val extension = extensions.create<NativeExportExtension>("nativeExport")
        configure<LibraryExtension> {
            libraryVariants.all {
                val variant: LibraryVariant = this

                configurations.getByName("${variant.name}ApiElements").outgoing.let { outgoing ->
                    extension.cppHeaderDirs.forEach { headerDir ->
                        outgoing.artifact(headerDir) {
                            type = ArtifactType.CppHeaderDir
                        }
                    }
                }

                configurations.getByName("${variant.name}RuntimeElements").outgoing.let { outgoing ->
                    outgoing.artifact(file("nativeLibNames.lst")) {
                        type = ArtifactType.LibNames
                    }
                }
            }
        }
    }
}
