package fr.o80.twitck.lib.internal.service.topic

import fr.o80.twitck.lib.api.service.TwitchApi
import fr.o80.twitck.lib.api.service.log.LoggerFactory

class TopicManager(
    private val userId: String,
    private val ngrokTunnel: NgrokTunnel,
    private val api: TwitchApi,
    private val secret: String,
    private val webhooksServer: WebhooksServer,
    loggerFactory: LoggerFactory
) {

    private val logger = loggerFactory.getLogger(TopicManager::class)

    fun subscribe() {
            val callbackUrl = ngrokTunnel.getOrOpenTunnel()
            webhooksServer.start()
            subscribeToTopics(callbackUrl)
    }

    private fun subscribeToTopics(callbackUrl: String) {
        api.validate()

        Topic.values().forEach { topic ->
            logger.info("Subscribing to $topic...")
            api.unsubscribeFrom(
                topic = topic.topicUrl(userId),
                callbackUrl = topic.callbackUrl(callbackUrl),
                leaseSeconds = topic.leaseDuration.toSeconds()
            )
            api.subscribeTo(
                topic = topic.topicUrl(userId),
                callbackUrl = topic.callbackUrl(callbackUrl),
                leaseSeconds = topic.leaseDuration.toSeconds(),
                secret = secret
            )
        }
    }
}