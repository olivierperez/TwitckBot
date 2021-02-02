package fr.o80.twitck.extension.points

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.HelpExtension
import fr.o80.twitck.lib.api.extension.PointsExtension
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.internal.service.ConfigService

class DefaultPointsExtension(
    override val channel: String,
    private val extensionProvider: ExtensionProvider,
    private val bank: PointsBank
) : PointsExtension {

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

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): PointsExtension {
            val config = configService.getConfig("points.json", PointsConfiguration::class)

            val bank = PointsBank(serviceLocator.extensionProvider)
            val logger = serviceLocator.loggerFactory.getLogger(DefaultPointsExtension::class)
            val storage = serviceLocator.extensionProvider.first(StorageExtension::class)

            val pointsCommands = PointsCommands(
                config.channel,
                config.privilegedBadges,
                config.i18n,
                bank,
                logger,
                storage
            )
            return DefaultPointsExtension(
                config.channel,
                serviceLocator.extensionProvider,
                bank
            )
                .also { points ->
                    pipeline.requestChannel(points.channel)
                    pipeline.interceptCommandEvent { messenger, commandEvent ->
                        pointsCommands.interceptCommandEvent(messenger, commandEvent)
                    }
                    points.onInstallationFinished()
                }
        }
    }

}