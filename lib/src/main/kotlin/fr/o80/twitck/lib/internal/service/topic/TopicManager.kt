package fr.o80.twitck.lib.internal.service.topic

import fr.o80.twitck.lib.api.extension.TunnelExtension
import fr.o80.twitck.lib.api.service.TwitchApi
import fr.o80.twitck.lib.api.service.log.LoggerFactory

class TopicManager(
    private val userId: String,
    private val tunnel: TunnelExtension,
    private val api: TwitchApi,
    private val secret: String,
    private val webhooksServer: WebhooksServer,
    loggerFactory: LoggerFactory
) {

    private val logger = loggerFactory.getLogger(TopicManager::class)

    fun subscribe() {
        val callbackUrl = tunnel.getTunnelUrl()
        webhooksServer.start()
        subscribeToTopics(callbackUrl)
    }

    private fun subscribeToTopics(callbackUrl: String) {
        api.validate()

        Topic.values().forEach { topic ->
            logger.info("Unsubscribing from $topic...")
            val unsubscribeResponse = api.unsubscribeFrom(
                topic = topic.topicUrl(userId),
                callbackUrl = topic.callbackUrl(callbackUrl),
                leaseSeconds = topic.leaseDuration.seconds,
                secret = secret
            )
            logger.trace("Unsubscribe response: \"$unsubscribeResponse\"")

            logger.info("Subscribing to $topic...")
            val subscribeResponse = api.subscribeTo(
                topic = topic.topicUrl(userId),
                callbackUrl = topic.callbackUrl(callbackUrl),
                leaseSeconds = topic.leaseDuration.seconds,
                secret = secret
            )
            logger.trace("Subscribe response: \"$subscribeResponse\"")
        }
    }
}