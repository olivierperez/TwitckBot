package fr.o80.twitck.extension.ngrok

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.TunnelExtension
import fr.o80.twitck.lib.api.service.ConfigService
import fr.o80.twitck.lib.api.service.ServiceLocator

class NgrokTunnelExtension : TunnelExtension {

    private val ngrokTunnel = NgrokTunnel("TwitckBot", 9014)

    override fun getTunnelUrl(): String {
        return ngrokTunnel.getOrOpenTunnel()
    }

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): TunnelExtension? {
            configService.getConfig("ngrok.json", Any::class)
                ?.takeIf { it.enabled }
                ?: return null

            serviceLocator.loggerFactory.getLogger(NgrokTunnelExtension::class)
                .info("Installing Ngrok extension...")

            return NgrokTunnelExtension()
        }
    }

}
