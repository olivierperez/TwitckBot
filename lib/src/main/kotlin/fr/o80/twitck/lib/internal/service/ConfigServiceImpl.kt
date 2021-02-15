package fr.o80.twitck.lib.internal.service

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import fr.o80.twitck.lib.api.service.ConfigService
import fr.o80.twitck.lib.api.service.step.*
import fr.o80.twitck.lib.api.service.step.CommandStep
import fr.o80.twitck.lib.api.service.step.MessageStep
import fr.o80.twitck.lib.api.service.step.OverlayEventStep
import fr.o80.twitck.lib.api.service.step.OverlayPopupStep
import fr.o80.twitck.lib.api.service.step.SoundStep
import fr.o80.twitck.lib.internal.bean.ExtensionConfig
import fr.o80.twitck.lib.internal.json.ChannelNameAdapter
import fr.o80.twitck.lib.internal.json.ColorAdapter
import java.io.File
import kotlin.reflect.KClass

internal class ConfigServiceImpl(
    private val configDirectory: File
) : ConfigService {

    private val moshi = Moshi.Builder()
        .add(
            PolymorphicJsonAdapterFactory.of(ActionStep::class.java, "type")
                .withSubtype(CommandStep::class.java, ActionStep.Type.COMMAND.value)
                .withSubtype(MessageStep::class.java, ActionStep.Type.MESSAGE.value)
                .withSubtype(OverlayPopupStep::class.java, ActionStep.Type.OVERLAY_POPUP.value)
                .withSubtype(OverlayEventStep::class.java, ActionStep.Type.OVERLAY_EVENT.value)
                .withSubtype(SoundStep::class.java, ActionStep.Type.SOUND.value)
        )
        .add(ColorAdapter())
        .add(ChannelNameAdapter())
        .build()

    override fun <T : Any> getConfig(file: String, clazz: KClass<T>): ExtensionConfig<T>? {
        val types = Types.newParameterizedType(ExtensionConfig::class.java, clazz.java)
        return moshi.adapter<ExtensionConfig<T>>(types)!!
            .fromJson(File(configDirectory, file).readText())
    }

}