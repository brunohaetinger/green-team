plugins {
    java
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
    compileOnly("org.apache.flink:flink-core:2.2.0")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
}

tasks.jar {
    archiveFileName.set("customStatusListener-${project.version}.jar")
}