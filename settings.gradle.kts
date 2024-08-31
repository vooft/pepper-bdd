rootProject.name = "pepper-bdd"

include(
    ":pepper-bdd-core",
    ":pepper-bdd-sample"
)

includeBuild("pepper-bdd-gradle") {
    dependencySubstitution {
        substitute(module("io.github.vooft:pepper-bdd-gradle")).using(project(":"))
    }
}
includeBuild("pepper-bdd-compiler-plugin")
