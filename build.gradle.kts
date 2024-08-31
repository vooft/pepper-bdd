plugins {
    `pepper-bdd-base`
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.github.vooft:pepper-bdd-gradle")
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}
