


plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    id("com.gradle.plugin-publish") version "1.2.1"
    id("com.vanniktech.maven.publish") version "0.34.0"

}

group = "io.github.umutcansu.resolvedartifactsexporter"
version = "1.0.4"

java {
    withJavadocJar()
    withSourcesJar()
}

mavenPublishing {
    pom {
        name = "Resolved Artifacts Exporter Gradle Plugin"
        description = "A plugin to find and export resolved dependencies to a Maven repository."
        url = "https://github.com/umutcansu/Resolved-Artifacts-Exporter"
        licenses {
            license {
                name.set("The MIT License")
                url.set("http://www.opensource.org/licenses/mit-license.php")
            }
        }
        developers {
            developer {
                id = "umutcansu"
                name = "Umut Cansu"
                email = "umutcansu@gmail.com"
            }
        }
        scm {
            url = "https://github.com/umutcansu/Resolved-Artifacts-Exporter"
            connection = "scm:git:git://github.com/umutcansu/Resolved-Artifacts-Exporter.git"
        }
    }

    signAllPublications()
}

mavenPublishing {
    publishToMavenCentral(false)
}

gradlePlugin {
    website = "https://github.com/umutcansu"
    vcsUrl = "https://github.com/umutcansu/Resolved-Artifacts-Exporter.git"
    plugins {
        create("resolvedArtifactsExporterPlugin") {
            id = "io.github.umutcansu.resolvedartifactsexporter"
            implementationClass = "io.github.umutcansu.resolvedartifactsexporter.ResolvedArtifactsExporterPlugin"

            displayName = "Resolved Artifacts Exporter Plugin"
            description = "A plugin to find and export resolved dependencies to a Maven repository."
            tags = listOf("dependency", "exporter", "maven", "nexus", "repository")
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
    mavenCentral()
}

