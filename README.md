# Resolved Artifacts Exporter Gradle Plugin
A Gradle plugin that finds, filters, and uploads all resolved dependencies (.jar/.aar) of an Android or any JVM project to a specified private Maven repository (such as Nexus, Artifactory, etc.).

This plugin is designed to automate and centralize dependency management, which is especially useful in corporate environments or for systems with restricted internet access.

Why Use This Plugin?
In large development teams, centralizing all third-party libraries into a private repository is a critical need. This plugin automates the entire process with a single command.

🏢 Centralized Dependency Management: Ensures consistency by gathering all third-party libraries used by teams in a single, controlled place.

✈️ Offline Builds: Enables CI/CD servers without internet access to fetch all required libraries from the local network.

🛡️ Security Scanning: Facilitates centralized security scanning of all ingested third-party libraries before they are used by developers.

🏆 Creating a "Golden Repository": Helps in creating a "golden set" of approved and trusted libraries, enhancing build security and compliance.

⚡ Improved Build Speeds: Speeds up CI/CD processes by fetching dependencies from a fast internal network instead of slower, public repositories.

✨ Features
Finds all external and transitive dependencies of a project.

Intelligently ignores the project's own internal modules (subprojects).

Supports both .jar (Java/Kotlin libraries) and .aar (Android libraries) files.

Provides powerful include/exclude filtering based on groupId using Regular Expressions.

Uploads artifacts to any Maven-compatible repository using standard HTTP PUT with Basic Authentication.

🚀 Setup and Usage
1. Apply the Plugin
In your app module's build.gradle.kts file, add the plugin to your plugins block.

// app/build.gradle.kts


```kotlin
// build.gradle.kts (app module)
plugins {
    id("com.thell.resolvedartifactsexporter") version "1.0.0"
}
```

2. Configure the Plugin
In the same build.gradle.kts file, create the artifactsExporter block to configure the plugin's settings.

// app/build.gradle.kts
```kotlin
artifactsExporter {
    // [Required] The URL of your target Maven repository.
    url.set("http://your-nexus-server.com/repository/your-repo/")

    // [Required] Credentials for the repository.
    // Reading these from a local gradle.properties file is the best practice.
    username.set(System.getProperty("nexusUser", "admin"))
    password.set(System.getProperty("nexusPassword"))

    // [Optional] A list of regex patterns for groupIds to exclude.
    // Example: Exclude all Google, AndroidX, and JetBrains libraries.
    excludeGroups.set(listOf(
        "com\\.google\\..*",
        "androidx\\..*",
        "org\\.jetbrains(\\..*)?.*",
        "junit\\.*",
        "org\\.hamcrest\\.*"
    ))

    // [Optional] To include only specific groups.
    // includeGroups.set(listOf("com\\.squareup\\..*"))
}
```

3. Run the Task
After completing the configuration, run the following command from your project's terminal:

```bash
./gradlew exportArtifacts
```

Parameter	Type	Required?	Description
url	Property<String>	Yes	The full URL of the target Maven repository.
username	Property<String>	Yes	The username for repository authentication.
password	Property<String>	Yes	The password for repository authentication.
includeGroups	ListProperty<String>	No	A list of regex patterns. If not empty, only artifacts with a matching groupId will be included.
excludeGroups	ListProperty<String>	No	A list of regex patterns. Artifacts with a matching groupId will be excluded.

## ⚙️ Configuration Options

| Parameter       | Type                  | Required? | Description                                                                        |
| --------------- | --------------------- | :-------: | ---------------------------------------------------------------------------------- |
| `url`           | `Property<String>`    |    Yes    | The full URL of the target Maven repository.                                       |
| `username`      | `Property<String>`    |    Yes    | The username for repository authentication.                                        |
| `password`      | `Property<String>`    |    Yes    | The password for repository authentication.                                        |
| `includeGroups` | `ListProperty<String>` |    No     | A list of regex patterns. If not empty, only artifacts with a matching `groupId` will be included. |
| `excludeGroups` | `ListProperty<String>` |    No     | A list of regex patterns. Artifacts with a matching `groupId` will be excluded.    |
