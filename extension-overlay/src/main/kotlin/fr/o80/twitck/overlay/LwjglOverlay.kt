package fr.o80.twitck.overlay

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.overlay.graphics.OverlayWindow
import fr.o80.twitck.overlay.graphics.ext.Vertex3f
import fr.o80.twitck.overlay.graphics.renderer.PopupImageRenderer
import fr.o80.twitck.overlay.graphics.renderer.InformationRenderer
import java.io.InputStream
import java.time.Duration

class LwjglOverlay(
    windowName: String,
    informationText: String?,
    logger: Logger
) : OverlayExtension {

    private val width = 1920
    private val height = 1080
    private val greenBackgroundColor = Vertex3f(0f, 0.6f, 0f)
    private val textBackgroundColor = Vertex3f(0.8f, 0.8f, 0.8f)
    private val borderColor = Vertex3f(0.5f, 0.5f, 0.5f)
    private val textColor = Vertex3f(0.1f, 0.1f, 0.1f)

    private val overlay = OverlayWindow(
        title = windowName,
        width = width,
        height = height,
        bgColor = greenBackgroundColor,
        updatesPerSecond = 55,
        logger
    )

    private val informationRenderer = InformationRenderer(
        height = height,
        width = width,
        backgroundColor = textBackgroundColor,
        borderColor = borderColor,
        textColor = textColor
    )

    private val popupImageRenderer = PopupImageRenderer(
        height = height,
        width = width,
        backgroundColor = textBackgroundColor,
        borderColor = borderColor,
        textColor = textColor
    )

    init {
        informationRenderer.setText(informationText)
    }

    override fun alert(text: String, duration: Duration) {
        informationRenderer.popAlert(text, duration)
    }

    override fun showImage(path: InputStream, duration: Duration) {
        popupImageRenderer.setImage(path, null, duration)
    }

    override fun showImage(path: InputStream, text: String, duration: Duration) {
        popupImageRenderer.setImage(path, text, duration)
    }

    private fun start() {
        Thread(overlay).start()
        overlay.registerRender(informationRenderer)
        overlay.registerRender(popupImageRenderer)
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