plugins {
    java
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("org.slf4j:slf4j-simple:2.0.13")
    implementation("io.openlineage:openlineage-java:1.45.0")
}

application {
    mainClass.set("com.lineage.Main")
}
