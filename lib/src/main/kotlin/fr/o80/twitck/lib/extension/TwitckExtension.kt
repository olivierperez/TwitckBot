package fr.o80.twitck.lib.extension

import fr.o80.twitck.lib.Pipeline

interface TwitckExtension<Configuration, Feature> {
    fun install(pipeline: Pipeline, configure: Configuration.() -> Unit): Feature
}
