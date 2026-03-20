plugins {
    java
    application
    id("com.gradleup.shadow") version "9.3.2"
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


group = "com.lineage"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.lineage.Main")
}


tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("kafka-connect-lineage-emitter-${project.version}-fat.jar")
        manifest {
            attributes["Main-Class"] = "com.lineage.Main"
        }
    }

    assemble.get().dependsOn(shadowJar)
}
