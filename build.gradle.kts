allprojects {
    group = "io.github.vooft"
    version = System.getenv("TAG") ?: "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

