package fr.o80.twitck.overlay

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.Overlay
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.overlay.graphics.OverlayWindow
import fr.o80.twitck.overlay.graphics.ext.Vertex3f
import fr.o80.twitck.overlay.graphics.renderer.InformationRenderer
import java.time.Duration

class StandardOverlay(
    windowName: String,
    logger: Logger
) : Overlay {

    private val width = 512
    private val height = 110
    private val bgColor = Vertex3f(0.8f, 0.8f, 0.8f)
    private val borderColor = Vertex3f(0.5f, 0.5f, 0.5f)
    private val textColor = Vertex3f(0.1f, 0.1f, 0.1f)

    private val overlay = OverlayWindow(windowName, width, height, 55, logger)
    private val renderer = InformationRenderer(width, height, bgColor, borderColor, textColor)

    private val informationTexts = mutableMapOf<String, List<String>>()

    // TODO OPZ Est-ce que ça a du sens de donner une priorité à certains textes ?
    // TODO OPZ Faire changer le text toutes les X minutes ?
    override fun provideInformation(namespace: String, texts: List<String>) {
        informationTexts[namespace] = texts
        renderer.updateTexts(informationTexts.entries.flatMap { (_, value) -> value })
    }

    override fun alert(text: String, duration: Duration) {
        renderer.popAlert(text, duration)
    }

    private fun start() {
        Thread(overlay).start()
        overlay.registerRender(renderer)
    }

    class Configuration {
        fun build(serviceLocator: ServiceLocator): StandardOverlay {
            val logger = serviceLocator.loggerFactory.getLogger(StandardOverlay::class)
            return StandardOverlay(
                windowName = "Streaming Overlay",
                logger = logger
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, StandardOverlay> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): StandardOverlay {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { overlay ->
                    overlay.start()
                }
        }

    }
}