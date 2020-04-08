apply(from = "../dependencies.gradle")

plugins {
    `kotlin-dsl`
}

repositories {
    google()
    jcenter()
}

dependencies {
    implementation("com.android.tools.build:gradle:3.6.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${versionOf("Kotlin")}")
}

fun versionOf(dependency: String) = (rootProject.extensions["Versions"] as ExtraPropertiesExtension)[dependency]
