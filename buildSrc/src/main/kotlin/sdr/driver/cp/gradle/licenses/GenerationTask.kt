package sdr.driver.cp.gradle.licenses

import com.android.build.gradle.AppExtension
import com.android.ide.common.symbols.getPackageNameFromManifest
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.kotlin.dsl.the
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import java.io.File

internal fun Project.createGenerationTask(
    dependenciesTask: Task,
    dependenciesFile: File,
    extension: LicensesExtension
) = tasks.create("generateLicenses") {
    inputs.files(dependenciesTask.outputs)
    afterEvaluate {
        inputs.property("local dependencies", extension.localDependencies.toString())
        inputs.property("permitted licenses", extension.permittedLicenses.toString())
    }

    val mainPackageName = getMainPackageName()

    val generatedSourceDir = File(
        buildDir,
        "generated/source/licenses/${mainPackageName.replace('.', '/')}/"
    )
    val objectName = "Dependencies"
    val generatedSourceFile = File(generatedSourceDir, "$objectName.kt")

    outputs.file(generatedSourceFile)

    this@createGenerationTask.the<AppExtension>().applicationVariants.all {
        registerJavaGeneratingTask(this@create, generatedSourceDir)
    }

    doLast {
        val dependencies: ArrayList<DependencyData> =
            getPomsData(dependenciesFile).apply {
                addAll(extension.localDependencies)
                sort()
            }

        val dependenciesSourceFileGenerator = DependenciesSourceFileGenerator(
            outputFile = generatedSourceFile,
            packageName = mainPackageName,
            objectName = objectName
        )

        dependencies.forEachUnique {
            dependenciesSourceFileGenerator.add(
                when (it) {
                    is DependencyData.Final -> it
                    is DependencyData.Pom -> resolveLicense(it, extension.permittedLicenses)
                }
            )
        }

        dependenciesSourceFileGenerator.close()
    }
}

private fun Project.getMainPackageName(): String = getPackageNameFromManifest(
    the<AppExtension>()
        .sourceSets.getByName("main")
        .manifest.srcFile
)

private fun Project.getPomsData(dependenciesFile: File): ArrayList<DependencyData> {
    val pomsData = ArrayList<DependencyData>()

    val pomParser = PomParser()
    forEachDependencyIn(dependenciesFile) { moduleGroup, moduleName, moduleVersion ->
        val pomFile = resolvePom(moduleGroup, moduleName, moduleVersion)
        pomParser.parse(pomFile)
        pomsData.add(
            DependencyData.Pom(
                name = pomParser.name,
                url = pomParser.url,
                licenses = pomParser.licenses,
                moduleGroup = moduleGroup,
                moduleName = moduleName,
                moduleVersion = moduleVersion
            )
        )
    }

    return pomsData
}

private fun ArrayList<out DependencyData>.sort() = sortWith { a, b ->
    var d = a.name.compareTo(b.name, ignoreCase = true)
    if (d != 0)
        return@sortWith d
    d = a.url.compareTo(b.url)
    if (d != 0)
        return@sortWith d
    when (a) {
        is DependencyData.Final -> when (b) {
            is DependencyData.Final -> 0
            is DependencyData.Pom -> -1
        }
        is DependencyData.Pom -> when (b) {
            is DependencyData.Final -> 1
            is DependencyData.Pom -> 0
        }
    }
}

private inline fun List<DependencyData>.forEachUnique(action: (DependencyData) -> Unit) {
    var lastDependencyData: DependencyData = DependencyData.Final("", "", "")
    forEach {
        if (it.name != lastDependencyData.name || it.url != lastDependencyData.url) {
            lastDependencyData = it
            action(it)
        }
    }
}

private fun Project.resolvePom(
    moduleGroup: String,
    moduleName: String,
    moduleVersion: String
): File {
    val artifact = dependencies.createArtifactResolutionQuery()
        .forModule(moduleGroup, moduleName, moduleVersion)
        .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
        .execute()
        .resolvedComponents
        .first()
        .getArtifacts(MavenPomArtifact::class.java)
        .single() as ResolvedArtifactResult
    return artifact.file
}

private fun resolveLicense(
    pomData: DependencyData.Pom,
    permittedLicenses: List<PermittedLicense>
): DependencyData.Final {
    permittedLicenses.forEach { permittedLicense ->
        pomData.licenses.forEach { pomLicense ->
            if (permittedLicense.name == pomLicense.name)
                return DependencyData.Final(
                    name = pomData.name,
                    url = pomData.url,
                    licenseSlug = permittedLicense.slug
                )
        }
    }
    throw Exception("No permitted license for dependency ${pomData.moduleIdentifier}. The options are ${pomData.licenses}.")
}
