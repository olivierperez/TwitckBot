package fr.o80.twitck.overlay

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.internal.service.ConfigService
import fr.o80.twitck.overlay.graphics.OverlayWindow
import fr.o80.twitck.overlay.graphics.ext.Vertex3f
import fr.o80.twitck.overlay.graphics.renderer.InformationRenderer
import fr.o80.twitck.overlay.graphics.renderer.PopupImageRenderer
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
        // TODO OPZ Remettre : Thread(overlay).start()
        overlay.registerRender(informationRenderer)
        overlay.registerRender(popupImageRenderer)
    }

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): OverlayExtension {
            val configuration = configService.getConfig("overlay.json", OverlayConfiguration::class)
            val logger = serviceLocator.loggerFactory.getLogger(LwjglOverlay::class)

            return LwjglOverlay(
                windowName = "Streaming Overlay",
                informationText = configuration.informationText,
                logger = logger
            ).also { overlay ->
                overlay.start()
            }
        }
    }

}
