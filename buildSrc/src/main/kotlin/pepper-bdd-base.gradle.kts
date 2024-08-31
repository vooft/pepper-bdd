import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
    id("org.jmailen.kotlinter")
}

dependencies {
    addPlatform(project, platform("org.jetbrains.kotlin:kotlin-bom:${getKotlinPluginVersion()}"))
}

tasks.withType<Detekt> {
    buildUponDefaultConfig = true
    config.from(files("${rootDir.absolutePath}/detekt.yaml"))
    basePath = rootDir.absolutePath

    exclude { it.file.absolutePath.contains("generated") }

    tasks.getByName("check").dependsOn(this)
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = mutableSetOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        showStandardStreams = true
        showExceptions = true
    }
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

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xcontext-receivers")
        allWarningsAsErrors = true
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

private fun DependencyHandler.addPlatform(project: Project, platform: Dependency) {
    val availableConfigurations = project.configurations.map { it.name }.toSet()
    availableConfigurations.intersect(
        setOf("api", "implementation", "testImplementation")
    ).forEach { configuration ->
        add(configuration, platform)
    }
}

private fun <D : Dependency> DependencyHandler.addPlatform(project: Project, platform: Provider<D>) = addPlatform(project, platform.get())
