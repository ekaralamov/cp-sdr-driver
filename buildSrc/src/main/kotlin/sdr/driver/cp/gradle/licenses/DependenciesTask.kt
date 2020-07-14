package sdr.driver.cp.gradle.licenses

import com.android.build.gradle.AppExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.kotlin.dsl.the
import java.io.File

internal fun Project.createDependenciesTask(
    extension: LicensesExtension,
    dependenciesFile: File
) = tasks.create("determineReleaseRuntimeDependencies") {
    outputs.file(dependenciesFile)
    outputs.upToDateWhen { false }

    doLast {
        determineDependencies(extension.releaseFlavor)
            .toSortedList()
            .writeTo(dependenciesFile)
    }
}

private fun Project.determineDependencies(releaseFlavor: String): Set<ResolvedDependency> {
    val result = HashSet<ResolvedDependency>()

    val rootProjectPrefix = "${rootProject.name}."

    val releaseVariant =
        this@determineDependencies.the<AppExtension>().applicationVariants.single { it.flavorName == releaseFlavor && it.buildType.name == "release" }

    releaseVariant.runtimeConfiguration.resolvedConfiguration.firstLevelModuleDependencies.recursiveForEach {
        if (!moduleGroup.startsWith(rootProjectPrefix))
            result += this
    }

    return result
}

private fun Set<ResolvedDependency>.toSortedList(): List<ResolvedDependency> {
    val result = ArrayList(this)
    result.sortWith { a, b ->
        var d = a.moduleGroup.compareTo(b.moduleGroup)
        if (d != 0)
            return@sortWith d
        d = a.moduleName.compareTo(b.moduleName)
        if (d != 0)
            return@sortWith d
        a.moduleVersion.compareTo(b.moduleVersion)
    }
    return result
}

private fun Set<ResolvedDependency>.recursiveForEach(action: ResolvedDependency.() -> Unit) {
    forEach {
        it.action()
        it.children.recursiveForEach(action)
    }
}
