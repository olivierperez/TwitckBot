package fr.o80.twitck.lib.api.bean

import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.internal.PipelineProvider

internal class TwitckConfiguration(
    val oauthToken: String,
    val hostName: String,
    pipeline: PipelineProvider,
    serviceLocator: ServiceLocator
) : ServiceLocator by serviceLocator,
    PipelineProvider by pipeline
