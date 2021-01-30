package fr.o80.twitck.extension.points

import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.HelpExtension
import fr.o80.twitck.lib.api.extension.PointsManager
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.ServiceLocator

class Points(
    override val channel: String,
    private val extensionProvider: ExtensionProvider,
    private val commands: PointsCommands,
    private val bank: PointsBank
) : PointsManager {

    override fun getPoints(login: String): Int {
        return bank.getPoints(login)
    }

    override fun addPoints(login: String, points: Int) {
        bank.addPoints(login, points)
    }

    override fun consumePoints(login: String, points: Int): Boolean {
        return bank.removePoints(login, points)
    }

    private fun onInstallationFinished() {
        extensionProvider.forEach(HelpExtension::class) { help ->
            help.registerCommand(POINTS_COMMAND)
            help.registerCommand(POINTS_GIVE_COMMAND)
        }
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null
        private var badges: Array<out Badge>? = null

        private var messages: Messages? = null

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @Dsl
        fun privilegedBadges(vararg badges: Badge) {
            if (badges.isEmpty()) {
                throw IllegalArgumentException("Impossible to set an empty list of privileged badges.")
            }
            this.badges = badges
        }

        @Dsl
        fun messages(
            destinationViewerDoesNotExist: String,
            pointsTransferred: String,
            noPointsEnough: String,
            viewHasNoPoints: String,
            viewHasPoints: String,
        ) {
            messages = Messages(
                destinationViewerDoesNotExist,
                pointsTransferred,
                noPointsEnough,
                viewHasNoPoints,
                viewHasPoints
            )
        }

        fun build(serviceLocator: ServiceLocator): Points {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Points::class.simpleName}")
            val privilegedBadges = badges
                ?: arrayOf(Badge.BROADCASTER)
            val theMessages = messages
                ?: throw IllegalStateException("Messages must be set for the extension ${Points::class.simpleName}")

            val bank = PointsBank(serviceLocator.extensionProvider)
            val storage = serviceLocator.extensionProvider.first(StorageExtension::class)
            val logger = serviceLocator.loggerFactory.getLogger(Points::class)

            val pointsCommands = PointsCommands(
                channelName,
                privilegedBadges,
                bank,
                theMessages,
                logger,
                storage
            )
            return Points(
                channelName,
                serviceLocator.extensionProvider,
                pointsCommands,
                bank
            )
        }
    }

    /*companion object Extension : ExtensionInstaller<Configuration, Points> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): Points {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { points ->
                    pipeline.interceptCommandEvent { messenger, commandEvent ->
                        points.commands.interceptCommandEvent(messenger, commandEvent)
                    }
                    pipeline.requestChannel(points.channel)
                    points.onInstallationFinished()
                }
        }

    }*/
}