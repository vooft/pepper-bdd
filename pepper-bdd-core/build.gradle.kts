plugins {
    `pepper-bdd-jvm`
    `pepper-bdd-publish`
}

dependencies {
    api(libs.kotest.framework.api)
    implementation(project(":pepper-bdd-reports:pepper-bdd-reports-builder"))
}
