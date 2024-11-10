plugins {
    `pepper-bdd-jvm`
    `pepper-bdd-publish`
}

dependencies {
    api(project(":pepper-bdd-reports:pepper-bdd-reports-api"))
    implementation(project(":pepper-bdd-reports:pepper-bdd-reports-builder"))
    implementation(libs.kotest.framework.api)
}
