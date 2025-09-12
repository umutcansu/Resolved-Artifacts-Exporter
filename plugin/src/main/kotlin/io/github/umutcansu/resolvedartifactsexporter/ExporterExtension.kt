package io.github.umutcansu.resolvedartifactsexporter


import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class ExporterExtension @Inject constructor() {
    abstract val url: Property<String>
    abstract val username: Property<String>
    abstract val password: Property<String>

    abstract val includeGroups: ListProperty<String>
    abstract val excludeGroups: ListProperty<String>

    init {
        includeGroups.convention(emptyList())
        excludeGroups.convention(emptyList())
    }
}