plugins {
    java
    id("com.gradleup.shadow") version "9.3.2"
}

group = "com.greenteam"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.flink:flink-streaming-java:2.2.0")
    implementation("org.apache.flink:flink-clients:2.2.0")
    implementation("org.apache.flink:flink-connector-kafka:4.0.1-2.0")
    implementation("org.apache.flink:flink-connector-base:2.2.0")

    implementation("org.apache.kafka:kafka-clients:3.6.1")
    implementation("org.apache.kafka:connect-api:3.6.1")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("enrichSales-${project.version}-fat.jar")

        manifest {
            attributes["Main-Class"] = "com.greenteam.EnrichSales"
        }
    }

    register<JavaExec>("run") {
        group = "verification"
        mainClass.set("com.greenteam.EnrichSales")
        classpath = sourceSets["main"].runtimeClasspath
    }
}

tasks.assemble.get().dependsOn(tasks.shadowJar)