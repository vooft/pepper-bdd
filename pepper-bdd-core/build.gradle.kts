plugins {
    `pepper-bdd-jvm`
    `pepper-bdd-publish`
}

dependencies {
    api(libs.kotest.framework.engine)
    api(libs.kotlin.reflect)
    implementation(project(":pepper-bdd-reports:pepper-bdd-reports-builder"))
}
