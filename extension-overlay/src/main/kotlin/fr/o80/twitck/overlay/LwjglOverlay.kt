package fr.o80.twitck.overlay

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.overlay.graphics.OverlayWindow
import fr.o80.twitck.overlay.graphics.ext.Vertex3f
import fr.o80.twitck.overlay.graphics.renderer.ImageRenderer
import fr.o80.twitck.overlay.graphics.renderer.InformationRenderer
import java.time.Duration

class LwjglOverlay(
    windowName: String,
    informationText: String?,
    logger: Logger
) : OverlayExtension {

    private val width = 1920
    private val height = 1080
    private val bgColor = Vertex3f(0f, 0.6f, 0f)
    private val borderColor = Vertex3f(0.5f, 0.5f, 0.5f)
    private val textColor = Vertex3f(0.1f, 0.1f, 0.1f)

    private val overlay = OverlayWindow(
        title = windowName,
        width = width,
        height = height,
        bgColor = bgColor,
        updatesPerSecond = 55,
        logger
    )

    private val informationRenderer = InformationRenderer(
        height = height,
        width = width,
        bgColor = Vertex3f(0.8f, 0.8f, 0.8f),
        borderColor = borderColor,
        textColor = textColor
    )

    private val imageRenderer = ImageRenderer(
        height = height,
        width = width
    )

    init {
        informationRenderer.setText(informationText)
    }

    override fun alert(text: String, duration: Duration) {
        informationRenderer.popAlert(text, duration)
    }

    override fun showImage(path: String, duration: Duration) {
        imageRenderer.setImage(path, duration)
    }

    private fun start() {
        Thread(overlay).start()
        overlay.registerRender(informationRenderer)
        overlay.registerRender(imageRenderer)
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var informationText: String? = null

        @Dsl
        fun informationText(text: String) {
            informationText = text
        }

        fun build(serviceLocator: ServiceLocator): LwjglOverlay {
            val logger = serviceLocator.loggerFactory.getLogger(LwjglOverlay::class)
            return LwjglOverlay(
                windowName = "Streaming Overlay",
                informationText = informationText,
                logger = logger
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, LwjglOverlay> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): LwjglOverlay {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { overlay ->
                    overlay.start()
                }
        }

    }
}