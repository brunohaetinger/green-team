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
    // Flink Core
    implementation("org.apache.flink:flink-streaming-java:2.2.0")
    
    // Flink Clients (for local execution)
    implementation("org.apache.flink:flink-clients:2.2.0")
    
    // Flink Kafka Connector
    implementation("org.apache.flink:flink-connector-kafka:4.0.1-2.0")
    
    // Flink Connector Base (for DeliveryGuarantee)
    implementation("org.apache.flink:flink-connector-base:2.2.0")
    
    // Kafka Clients
    implementation("org.apache.kafka:kafka-clients:3.6.1")
    implementation("org.apache.kafka:connect-api:3.6.1")
    
    // JSON Processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // OpenLineage Flink integration
    implementation("io.openlineage:openlineage-flink:1.45.0")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("${project.name}-${project.version}-fat.jar")
        
        manifest {
            attributes["Main-Class"] = "com.greenteam.TopSalesman"
        }
    }
    
    register<JavaExec>("run") {
        group = "verification"
        mainClass.set("com.greenteam.TopSalesman")
        classpath = sourceSets["main"].runtimeClasspath
    }
}

// Make shadowJar the default jar
tasks.assemble.get().dependsOn(tasks.shadowJar)
