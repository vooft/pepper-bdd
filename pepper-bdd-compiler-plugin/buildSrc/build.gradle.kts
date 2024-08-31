plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")

    // detekt / ktlint
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${libs.plugins.detekt.get().version}")
    implementation("org.jmailen.gradle:kotlinter-gradle:${libs.plugins.ktlint.get().version}")

    // publishing
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:${libs.plugins.dokka.get().version}")
    implementation("com.vanniktech:gradle-maven-publish-plugin:${libs.plugins.maven.central.publish.get().version}")
}
