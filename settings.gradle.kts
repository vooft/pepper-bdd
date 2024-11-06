rootProject.name = "pepper-bdd"

include(
    ":pepper-bdd-core",
    ":pepper-bdd-sample",
    ":pepper-bdd-compiler-plugin",
    ":pepper-bdd-reports:pepper-bdd-reports-api",
    ":pepper-bdd-reports:pepper-bdd-reports-builder"
)

includeBuild("pepper-bdd-gradle") {
    dependencySubstitution {
        substitute(module("io.github.vooft:pepper-bdd-gradle")).using(project(":"))
    }
}
