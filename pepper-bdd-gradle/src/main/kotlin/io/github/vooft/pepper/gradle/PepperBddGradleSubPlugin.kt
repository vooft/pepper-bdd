package io.github.vooft.pepper.gradle

import io.github.vooft.pepper_bdd_gradle.BuildConfig
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

open class PepperBddExtension {
    var enabled: Boolean = true
}

class PepperBddGradleSubPlugin : KotlinCompilerPluginSupportPlugin {

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val extension = kotlinCompilation.target.project.extensions.findByType(PepperBddExtension::class.java)
            ?: return kotlinCompilation.target.project.provider { emptyList() }

        return kotlinCompilation.target.project.provider { listOf(SubpluginOption("enabled", extension.enabled.toString())) }
    }

    override fun apply(target: Project) {
        target.extensions.create("pepper", PepperBddExtension::class.java)
    }

    override fun getCompilerPluginId(): String = "io.github.vooft.pepper-bdd-compiler"

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) = true

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "io.github.vooft",
        artifactId = "pepper-bdd-compiler-plugin",
        version = BuildConfig.VERSION,
    )

}
