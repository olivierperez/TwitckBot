package fr.o80.twitck.lib.extension

import fr.o80.twitck.lib.ExtensionProvider
import fr.o80.twitck.lib.Pipeline

interface TwitckExtension<Configuration, Feature> {
    fun install(pipeline: Pipeline, extensionProvider: ExtensionProvider, configure: Configuration.() -> Unit): Feature
}
