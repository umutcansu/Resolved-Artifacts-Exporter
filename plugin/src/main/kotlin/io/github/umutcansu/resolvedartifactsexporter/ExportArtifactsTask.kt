package io.github.umutcansu.resolvedartifactsexporter


import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

abstract class ExportArtifactsTask : DefaultTask() {

    @get:Input
    abstract val repoUrl: Property<String>
    @get:Input
    abstract val repoUsername: Property<String>
    @get:Input
    abstract val repoPassword: Property<String>
    @get:Input
    @get:Optional
    abstract val includeGroups: ListProperty<String>
    @get:Input
    @get:Optional
    abstract val excludeGroups: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val pathPrefix: Property<String>

    init {
        group = "Publishing"
        description = "Finds, filters, and exports resolved dependencies to a Maven repository via HTTP PUT."
    }

    @TaskAction
    fun execute() {
        val artifactsToUpload = project.configurations
            .filter { it.name.endsWith("RuntimeClasspath", ignoreCase = true) }
            .flatMap { it.resolvedConfiguration.resolvedArtifacts }
            .filter { it.id.componentIdentifier !is ProjectComponentIdentifier }
            .toSet()

        val includes = includeGroups.get().map { it.toRegex() }
        val excludes = excludeGroups.get().map { it.toRegex() }

        logger.lifecycle(">> Task received ${excludes.size} exclude patterns: ${excludes.map { it.pattern }}")

        val filteredArtifacts = artifactsToUpload.filter { artifact ->
            val group = artifact.moduleVersion.id.group
            var isExcluded = false

            logger.lifecycle("==> Checking group: '$group'")
            for (regex in excludes) {
                val isMatch = regex.matches(group)
                logger.lifecycle("    - against regex: '${regex.pattern}' -> match: $isMatch")
                if (isMatch) {
                    isExcluded = true
                    break
                }
            }

            if (isExcluded) return@filter false
            if (includes.isNotEmpty() && includes.none { it.matches(group) }) return@filter false
            true
        }

        logger.lifecycle(">> After filtering, ${filteredArtifacts.size} artifacts will be uploaded.")

        val prefix = pathPrefix.getOrElse("")
        filteredArtifacts.forEach { artifact ->
            uploadSingleArtifact(repoUrl.get(), repoUsername.get(), repoPassword.get(), prefix, artifact)
        }

        logger.lifecycle(">> All export operations completed.")
    }

    private fun uploadSingleArtifact(baseUrl: String, user: String, pass: String, prefix: String, artifact: ResolvedArtifact) {
        val module = artifact.moduleVersion.id
        val groupPath = module.group.replace('.', '/')

        val finalPrefix = if (prefix.isNotBlank()) "${prefix.trim('/')}/" else ""
        val artifactPath = "${baseUrl.removeSuffix("/")}/${finalPrefix}$groupPath/${module.name}/${module.version}"

        val artifactFileName = "${module.name}-${module.version}.${artifact.extension}"
        val pomFileName = "${module.name}-${module.version}.pom"

        val pomContent = """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <groupId>${module.group}</groupId>
          <artifactId>${module.name}</artifactId>
          <version>${module.version}</version>
        </project>
    """.trimIndent()

        val pomUrl = "$artifactPath/$pomFileName"
        val artifactUrl = "$artifactPath/$artifactFileName"

        if (httpPut(pomUrl, user, pass, pomContent.toByteArray())) {
            if (httpPut(artifactUrl, user, pass, artifact.file.readBytes())) {
                logger.lifecycle(">> Successfully uploaded: ${module.group}:${module.name}:${module.version}")
            }
        }
    }


    private fun httpPut(urlString: String, user: String, pass: String, data: ByteArray): Boolean {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.doOutput = true
            val auth = "$user:$pass"
            val encodedAuth = Base64.getEncoder().encodeToString(auth.toByteArray())
            connection.setRequestProperty("Authorization", "Basic $encodedAuth")
            connection.setRequestProperty("Content-Type", "application/octet-stream")
            connection.outputStream.use { it.write(data) }
            val responseCode = connection.responseCode
            if (responseCode < 200 || responseCode >= 300) {
                logger.error("!! Failed to upload to $urlString. Response: $responseCode ${connection.responseMessage}")
                return false
            }
            return true
        } catch (e: Exception) {
            logger.error("!! Exception during upload to $urlString: ${e.message}")
            return false
        }
    }
}