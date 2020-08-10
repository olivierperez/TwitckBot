package fr.o80.twitck.lib.extension

import fr.o80.twitck.lib.Pipeline
import fr.o80.twitck.lib.service.ServiceLocator

interface TwitckExtension<Configuration, Feature> {
    fun install(pipeline: Pipeline, serviceLocator: ServiceLocator, configure: Configuration.() -> Unit): Feature
}
