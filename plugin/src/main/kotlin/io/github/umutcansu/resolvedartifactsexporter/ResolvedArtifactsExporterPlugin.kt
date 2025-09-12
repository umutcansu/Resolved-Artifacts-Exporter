package io.github.umutcansu.resolvedartifactsexporter

import org.gradle.api.Plugin
import org.gradle.api.Project

class ResolvedArtifactsExporterPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("maven-publish")

        val extension = project.extensions.create("artifactsExporter", ExporterExtension::class.java)

        project.tasks.register("exportArtifacts", ExportArtifactsTask::class.java) { task ->
            task.repoUrl.set(extension.url)
            task.repoUsername.set(extension.username)
            task.repoPassword.set(extension.password)
            task.includeGroups.set(extension.includeGroups)
            task.excludeGroups.set(extension.excludeGroups)
        }
    }
}