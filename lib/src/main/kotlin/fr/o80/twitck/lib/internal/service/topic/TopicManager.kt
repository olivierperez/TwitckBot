package fr.o80.twitck.lib.internal.service.topic

import fr.o80.twitck.lib.api.service.TwitchApi
import fr.o80.twitck.lib.api.service.log.LoggerFactory

class TopicManager(
    private val ngrokTunnel: NgrokTunnel,
    private val api: TwitchApi,
    private val secret: String,
    private val webhooksServer: WebhooksServer,
    loggerFactory: LoggerFactory
) {

    private val logger = loggerFactory.getLogger(TopicManager::class)

    fun subscribe() {
        // TODO OPZ Kill du bot si ngrok n'est pas démarré
        val callbackUrl = ngrokTunnel.getOrOpenTunnel()
        webhooksServer.start()
        subscribeToTopics(callbackUrl)
    }

    private fun subscribeToTopics(callbackUrl: String) {
        api.validate()

        // TODO OPZ utiliser le UserName du broadcaster
        val hostUserId = api.getUser("gnu_coding_cafe").id

        Topic.values().forEach { topic ->
            logger.info("Subscribing to $topic...")
            api.unsubscribeFrom(
                topic = topic.topicUrl(hostUserId),
                callbackUrl = topic.callbackUrl(callbackUrl),
                leaseSeconds = topic.leaseDuration.toSeconds()
            )
            api.subscribeTo(
                topic = topic.topicUrl(hostUserId),
                callbackUrl = topic.callbackUrl(callbackUrl),
                leaseSeconds = topic.leaseDuration.toSeconds(),
                secret = secret
            )
        }
    }
}