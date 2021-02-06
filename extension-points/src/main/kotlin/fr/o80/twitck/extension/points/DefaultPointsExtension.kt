package fr.o80.twitck.extension.points

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.exception.ExtensionDependencyException
import fr.o80.twitck.lib.api.extension.HelpExtension
import fr.o80.twitck.lib.api.extension.PointsExtension
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.internal.service.ConfigService

class DefaultPointsExtension(
    override val channel: String,
    private val help: HelpExtension?,
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
        help?.run {
            registerCommand(POINTS_COMMAND)
            registerCommand(POINTS_GIVE_COMMAND)
        }
    }

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): PointsExtension? {
            val config = configService.getConfig("points.json", PointsConfiguration::class)
                ?.takeIf { it.enabled }
                ?: return null

            val logger = serviceLocator.loggerFactory.getLogger(DefaultPointsExtension::class)
            logger.info("Installing Help extension...")

            val storage = serviceLocator.extensionProvider.firstOrNull(StorageExtension::class)
                ?: throw ExtensionDependencyException("Points", "Storage")
            val help = serviceLocator.extensionProvider.firstOrNull(HelpExtension::class)

            val bank = PointsBank(storage)
            val pointsCommands = PointsCommands(
                config.data.channel,
                config.data.privilegedBadges,
                config.data.i18n,
                bank,
                logger,
                storage
            )
            return DefaultPointsExtension(
                config.data.channel,
                help,
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