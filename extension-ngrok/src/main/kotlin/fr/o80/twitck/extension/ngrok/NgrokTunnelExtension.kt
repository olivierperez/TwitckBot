package fr.o80.twitck.extension.ngrok

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.exception.ExtensionDependencyException
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.extension.TunnelExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.ConfigService

class NgrokTunnelExtension(
    name: String,
    port: Int,
    private val storage: NgrokStorage
) : TunnelExtension {

    private val ngrokTunnel = NgrokTunnel(name, port)

    private val previousUrl: String? = storage.getUrl()

    override fun getPreviousUrl(): String? {
        return previousUrl
    }

    override fun getTunnelUrl(): String {
        return ngrokTunnel.getOrOpenTunnel().also { url ->
            storage.update(url)
        }
    }

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): TunnelExtension? {
            val config = configService.getConfig("ngrok.json", NgrokConfiguration::class)
                ?.takeIf { it.enabled }
                ?: return null

            val logger = serviceLocator.loggerFactory.getLogger(NgrokTunnelExtension::class)
            logger.info("Installing Ngrok extension...")

            val storage = serviceLocator.extensionProvider.firstOrNull(StorageExtension::class)
                ?: throw ExtensionDependencyException("Ngrok", "Storage")

//            NgrokProcess(
//                config.data.path,
//                logger
//            ).launch()

            return NgrokTunnelExtension(
                config.data.name,
                config.data.port,
                NgrokStorage(storage)
            )
        }
    }

}
