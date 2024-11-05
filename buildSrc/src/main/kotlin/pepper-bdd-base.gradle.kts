
import gradle.kotlin.dsl.accessors._8366d5ee69aa579ad2a32fcdee94f415.detekt
import io.gitlab.arturbosch.detekt.Detekt
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("io.gitlab.arturbosch.detekt")
    id("org.jmailen.kotlinter")
}

group = "io.github.vooft"
version = System.getenv("TAG") ?: "1.0-SNAPSHOT"

detekt {
    buildUponDefaultConfig = true
    config.from(files("${rootDir.absolutePath}/detekt.yaml"))
    basePath = rootDir.absolutePath
}

tasks.withType<Detekt> {
    tasks.getByName("check").dependsOn(this)
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

tasks.withType<LintTask> {
    source("build.gradle.kts", "settings.gradle.kts")
    exclude {
        it.file.path.startsWith("${layout.buildDirectory.get()}") && !it.file.path.endsWith("gradle.kts")
    }
    dependsOn("formatKotlin")
}

tasks.withType<FormatTask> {
    source("build.gradle.kts", "settings.gradle.kts")
    exclude {
        it.file.path.startsWith("${layout.buildDirectory.get()}") && !it.file.path.endsWith("gradle.kts")
    }
}
