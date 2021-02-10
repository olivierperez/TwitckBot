package fr.o80.twitck.overlay

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.OverlayEvent
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.internal.service.ConfigService
import fr.o80.twitck.overlay.events.EventsHolder
import fr.o80.twitck.overlay.graphics.OverlayWindow
import fr.o80.twitck.overlay.graphics.ext.Vertex3f
import fr.o80.twitck.overlay.events.EventsLayer
import fr.o80.twitck.overlay.informative.InformativeLayer
import fr.o80.twitck.overlay.model.LwjglEvent
import fr.o80.twitck.overlay.popup.PopupImageLayer
import java.io.File
import java.io.InputStream
import java.time.Duration
import java.time.Instant

class LwjglOverlay(
    windowName: String,
    informativeText: InformativeText?,
    private val eventsConfiguration: EventsConfiguration?,
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

    private val informativeLayer: InformativeLayer? = informativeText?.let {
        InformativeLayer(
            height = height,
            width = width,
            backgroundColor = textBackgroundColor,
            borderColor = borderColor,
            textColor = textColor,
            anchor = informativeText.anchor
        )
    }

    private val eventsHolder = EventsHolder(8)
    private val eventsLayer: EventsLayer? = eventsConfiguration?.let {
        EventsLayer(
            style,
            eventsConfiguration
        )
    }

    private val popupImageLayer = PopupImageLayer(
        height = height,
        width = width,
        backgroundColor = textBackgroundColor,
        borderColor = borderColor,
        textColor = textColor
    )

    init {
        informativeLayer?.setText(informativeText?.text)
    }

    override fun alert(text: String, duration: Duration) {
        informativeLayer?.popAlert(text, duration)
    }

    override fun onEvent(event: OverlayEvent) {
        eventsConfiguration?.let {
            eventsHolder.record(event.toLwjglEvent(eventsConfiguration.secondsToLeave))
            eventsLayer?.update(eventsHolder.events)
        }
    }

    override fun showImage(path: String, duration: Duration) {
        val imageStream = getImageStream(path)
        popupImageLayer.setImage(imageStream, null, duration)
    }

    override fun showImage(path: String, text: String, duration: Duration) {
        val imageStream = getImageStream(path)
        popupImageLayer.setImage(imageStream, text, duration)
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
        eventsLayer?.let { overlay.registerRender(it) }
        informativeLayer?.let { overlay.registerRender(it) }
        overlay.registerRender(popupImageLayer)
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
                eventsConfiguration = config.data.events,
                logger = logger
            ).also { overlay ->
                overlay.start()
            }
        }
    }

    private fun OverlayEvent.toLwjglEvent(secondsToLeave: Long): LwjglEvent {
        return LwjglEvent(
            this.text,
            Instant.now(),
            Duration.ofSeconds(secondsToLeave)
            )
    }

}
