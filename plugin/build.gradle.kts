

plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    id("com.gradle.plugin-publish") version "1.2.1"
}


group = "com.thell.resolvedartifactsexporter"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
}

gradlePlugin {
    website = "https://github.com/umutcansu"

    plugins {
        create("resolvedArtifactsExporterPlugin") {
            id = "com.thell.resolvedartifactsexporter"
            implementationClass = "com.thell.ResolvedArtifactsExporterPlugin"

            displayName = "Resolved Artifacts Exporter Plugin"
            description = "A plugin to find and export resolved dependencies to a Maven repository."
            tags.set(listOf("dependency", "exporter", "maven", "nexus", "repository"))
        }
    }
}

publishing {
    repositories {
        maven {
            name = "Nexus"
            url = uri("http://localhost:8081/repository/maven-releases/")
            isAllowInsecureProtocol = true
            credentials {
                username = project.findProperty("nexusUser") as? String
                password = project.findProperty("nexusPassword") as? String
            }
        }
    }
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}



// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

gradlePlugin.testSourceSets.add(functionalTestSourceSet)

tasks.named<Task>("check") {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}

tasks.named<Test>("test") {
    // Use JUnit Jupiter for unit tests.
    useJUnitPlatform()
}
