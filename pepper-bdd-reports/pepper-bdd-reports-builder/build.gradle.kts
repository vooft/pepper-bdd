plugins {
    `pepper-bdd-jvm`
    `pepper-bdd-publish`
}

dependencies {
    implementation(project(":pepper-bdd-reports:pepper-bdd-reports-api"))
    implementation(libs.kotest.framework.api)
}
