package sdr.driver.cp.gradle

import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get

class ClientLibPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        apply(plugin = "com.android.library")
        apply(plugin = "kotlin-android")
        apply(plugin = "sdr.driver.cp.base")
        apply(plugin = "maven-publish")
        apply(plugin = "com.jfrog.bintray")

        dependencies {
            with(configurations["implementation"]) {
                invoke(
                    group = "org.jetbrains.kotlin",
                    name = "kotlin-stdlib",
                    version = versionOf("Kotlin")
                )
            }
        }

        configure<BintrayExtension> {
            user = findProperty("BINTRAY_USER") as String?
            key = findProperty("BINTRAY_KEY") as String?
            with(pkg) {
                repo = "maven"
                setLicenses("Apache-2.0")
                vcsUrl = "https://gitlab.com/ekaralamov/cp-sdr-driver"
            }
        }
    }
}
