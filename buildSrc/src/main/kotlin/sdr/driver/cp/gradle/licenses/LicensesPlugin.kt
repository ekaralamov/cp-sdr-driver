package sdr.driver.cp.gradle.licenses

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import java.io.File

sealed class DependencyData {
    abstract val name: String
    abstract val url: String

    internal class Pom(
        override val name: String,
        override val url: String,
        val licenses: List<License>,
        moduleGroup: String,
        moduleName: String,
        moduleVersion: String
    ) : DependencyData() {

        data class License(
            val name: String,
            val url: String
        )

        val moduleIdentifier = "$moduleGroup:$moduleName:$moduleVersion"
    }

    data class Final(
        override val name: String,
        override val url: String,
        val licenseSlug: String
    ) : DependencyData()
}

data class PermittedLicense(
    val name: String,
    val slug: String
)

open class LicensesExtension {
    lateinit var releaseFlavor: String
    var permittedLicenses: List<PermittedLicense> = emptyList()
    var localDependencies: List<DependencyData.Final> = emptyList()
}

class LicensesPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        val extension = extensions.create<LicensesExtension>("licenses")

        val dependenciesFile = File(buildDir, "intermediates/licenses/release_runtime_dependencies.txt")

        val dependenciesTask = createDependenciesTask(extension, dependenciesFile)

        createGenerationTask(dependenciesTask, dependenciesFile, extension)
    }
}
