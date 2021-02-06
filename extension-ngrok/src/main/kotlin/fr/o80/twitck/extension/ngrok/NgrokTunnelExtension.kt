package fr.o80.twitck.extension.ngrok

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.TunnelExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.internal.service.ConfigService

class NgrokTunnelExtension(
    name: String,
    port: Int,
    ngrokProcess: NgrokProcess
) : TunnelExtension {

    private val ngrokTunnel = NgrokTunnel(name, port)

    init {
        ngrokProcess.launch()
    }

    override fun getTunnelUrl(): String {
        return ngrokTunnel.getOrOpenTunnel()
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

            val ngrokProcess = NgrokProcess(
                config.data.path,
                config.data.token,
                logger
            )

            return NgrokTunnelExtension(
                config.data.name,
                config.data.port,
                ngrokProcess
            )
        }
    }

}
