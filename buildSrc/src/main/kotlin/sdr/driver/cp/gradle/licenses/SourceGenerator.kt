package sdr.driver.cp.gradle.licenses

import java.io.File
import java.util.*

internal class DependenciesSourceFileGenerator(
    outputFile: File,
    packageName: String,
    objectName: String
) {

    private val writer = outputFile.printWriter()

    private var noDependenciesYet = true

    private val licenseSlugs = TreeSet<String>()

    init {
        writer.print(
"""package $packageName

object $objectName {

    data class DependencyData(
        val name: String,
        val url: String,
        val license: License
    )

    val list: List<DependencyData> = listOf("""
        )
    }

    fun add(dependencyData: DependencyData.Final) {
        if (noDependenciesYet)
            noDependenciesYet = false
        else
            writer.print(',')

        writer.print(
"""
        DependencyData(
            name = "${dependencyData.name}",
            url = "${dependencyData.url}",
            license = License.${dependencyData.licenseSlug}
        )"""
        )

        licenseSlugs += dependencyData.licenseSlug
    }

    fun close() {
        writer.print(
"""
    )

    enum class License {"""
        )

        var firstLicense = true

        licenseSlugs.forEach {
            if (firstLicense)
                firstLicense = false
            else
                writer.print(',')

            writer.print(
"""
        $it"""
            )
        }

        writer.print(
"""
    }
}
"""
        )

        writer.close()
    }
}
