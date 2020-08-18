package fr.o80.twitck.lib.api.extension

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.service.ServiceLocator

interface TwitckExtension<Configuration, Feature> {
    fun install(pipeline: Pipeline, serviceLocator: ServiceLocator, configure: Configuration.() -> Unit): Feature
}
