package fr.o80.twitck.lib.internal.service

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import fr.o80.twitck.lib.internal.bean.ExtensionConfig
import java.io.File
import kotlin.reflect.KClass

interface ConfigService {
    fun <T : Any> getConfig(file: String, clazz: KClass<T>): T
}

class ConfigServiceImpl(
    private val configDirectory: File
) : ConfigService {

    private val moshi = Moshi.Builder().build()

    override fun <T : Any> getConfig(file: String, clazz: KClass<T>): T {
        val types = Types.newParameterizedType(ExtensionConfig::class.java, clazz.java)
        return moshi.adapter<ExtensionConfig<T>>(types)!!
            .fromJson(File(configDirectory, file).readText())!!.data
    }

}