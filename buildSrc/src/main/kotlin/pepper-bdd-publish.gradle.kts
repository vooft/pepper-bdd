plugins {
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    pom {
        name = "pepper-bdd"
        description = "Kotlin BDD testing framework"
        url = "https://github.com/vooft/pepper-bdd"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        scm {
            connection = "https://github.com/vooft/pepper-bdd"
            url = "https://github.com/vooft/pepper-bdd"
        }
        developers {
            developer {
                name = "pepper-bdd team"
            }
        }
    }
}
