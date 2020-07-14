package sdr.driver.cp.gradle.licenses

import org.gradle.api.artifacts.ResolvedDependency
import java.io.File

internal fun List<ResolvedDependency>.writeTo(dependenciesFile: File) =
    dependenciesFile.printWriter().use { writer ->
        forEach {
            writer.println(it.moduleGroup)
            writer.println(it.moduleName)
            writer.println(it.moduleVersion)
        }
    }

internal inline fun forEachDependencyIn(
    dependenciesFile: File,
    action: (moduleGroup: String, moduleName: String, moduleVersion: String) -> Unit
) = dependenciesFile.useLines {
    val linesIterator = it.iterator()
    while (linesIterator.hasNext()) {
        val moduleGroup = linesIterator.next()
        val moduleName = linesIterator.next()
        val moduleVersion = linesIterator.next()

        action(moduleGroup, moduleName, moduleVersion)
    }
}
