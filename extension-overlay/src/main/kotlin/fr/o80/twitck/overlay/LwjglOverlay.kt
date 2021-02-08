package fr.o80.twitck.overlay

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.Color
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.internal.service.ConfigService
import fr.o80.twitck.overlay.graphics.OverlayWindow
import fr.o80.twitck.overlay.graphics.ext.Vertex3f
import fr.o80.twitck.overlay.graphics.renderer.InformationRenderer
import fr.o80.twitck.overlay.graphics.renderer.PopupImageRenderer
import java.io.File
import java.io.InputStream
import java.time.Duration

class LwjglOverlay(
    windowName: String,
    informativeText: InformativeText?,
    logger: Logger,
    style: OverlayStyle
) : OverlayExtension {

    private val width = 1920
    private val height = 1080
    private val greenBackgroundColor = Vertex3f(0f, 0.6f, 0f)
    private val textBackgroundColor = style.backgroundColor.toVertex3f()
    private val borderColor = style.borderColor.toVertex3f()
    private val textColor = style.textColor.toVertex3f()

    private val overlay = OverlayWindow(
        title = windowName,
        width = width,
        height = height,
        bgColor = greenBackgroundColor,
        updatesPerSecond = 55,
        logger
    )

    private val informationRenderer: InformationRenderer? = informativeText?.let {
        InformationRenderer(
            height = height,
            width = width,
            backgroundColor = textBackgroundColor,
            borderColor = borderColor,
            textColor = textColor,
            anchor = informativeText.anchor
        )
    }

    private val popupImageRenderer = PopupImageRenderer(
        height = height,
        width = width,
        backgroundColor = textBackgroundColor,
        borderColor = borderColor,
        textColor = textColor
    )

    init {
        informationRenderer?.setText(informativeText?.text)
    }

    override fun alert(text: String, duration: Duration) {
        informationRenderer?.popAlert(text, duration)
    }

    override fun showImage(path: String, duration: Duration) {
        val imageStream = getImageStream(path)
        popupImageRenderer.setImage(imageStream, null, duration)
    }

    override fun showImage(path: String, text: String, duration: Duration) {
        val imageStream = getImageStream(path)
        popupImageRenderer.setImage(imageStream, text, duration)
    }

    private fun getImageStream(path: String): InputStream {
        val imageFile = File(path)
        return if (imageFile.isFile && imageFile.canRead()) {
            imageFile.inputStream()
        } else {
            LwjglOverlay::class.java.classLoader.getResourceAsStream(path)
                ?: throw IllegalArgumentException("Failed to load image for resources: $path")
        }
    }

    private fun start() {
        Thread(overlay).start()
        informationRenderer?.let { overlay.registerRender(it) }
        overlay.registerRender(popupImageRenderer)
    }

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): OverlayExtension? {
            val config = configService.getConfig("overlay.json", OverlayConfiguration::class)
                ?.takeIf { it.enabled }
                ?: return null

            val logger = serviceLocator.loggerFactory.getLogger(LwjglOverlay::class)
            logger.info("Installing Overlay extension...")

            return LwjglOverlay(
                windowName = "Streaming Overlay",
                informativeText = config.data.informativeText,
                style = config.data.style,
                logger = logger
            ).also { overlay ->
                overlay.start()
            }
        }
    }

}

private fun Color.toVertex3f(): Vertex3f {
    return Vertex3f(
        red / 255f,
        green / 255f,
        blue / 255f
    )
}
