package fr.o80.twitck.lib.internal.service

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import fr.o80.twitck.lib.api.service.step.CommandStep
import fr.o80.twitck.lib.api.service.step.MessageStep
import fr.o80.twitck.lib.api.service.step.OverlayStep
import fr.o80.twitck.lib.api.service.step.SoundStep
import fr.o80.twitck.lib.internal.bean.ExtensionConfig
import java.io.File
import kotlin.reflect.KClass

interface ConfigService {
    fun <T : Any> getConfig(file: String, clazz: KClass<T>): T
}

class ConfigServiceImpl(
    private val configDirectory: File
) : ConfigService {

    private val moshi = Moshi.Builder()
        .add(
            PolymorphicJsonAdapterFactory.of(CommandStep::class.java, "type")
                .withSubtype(SoundStep::class.java, CommandStep.Type.SOUND.value)
                .withSubtype(OverlayStep::class.java, CommandStep.Type.OVERLAY.value)
                .withSubtype(MessageStep::class.java, CommandStep.Type.MESSAGE.value)
        )
        .build()

    override fun <T : Any> getConfig(file: String, clazz: KClass<T>): T {
        val types = Types.newParameterizedType(ExtensionConfig::class.java, clazz.java)
        return moshi.adapter<ExtensionConfig<T>>(types)!!
            .fromJson(File(configDirectory, file).readText())!!.data
    }

}
