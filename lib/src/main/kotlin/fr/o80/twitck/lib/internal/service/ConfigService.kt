package fr.o80.twitck.lib.internal.service

import com.squareup.moshi.Moshi
import fr.o80.twitck.lib.internal.bean.PartialExtensionConfig
import java.io.File
import kotlin.reflect.KClass

interface ConfigService {
    fun getConfigsFrom(directory: File): List<Pair<File, PartialExtensionConfig>>
    fun <T : Any> getConfig(clazz: KClass<T>): T
}

class ConfigServiceImpl : ConfigService {

    private val moshi = Moshi.Builder().build()
    private val partialAdapter = moshi.adapter(PartialExtensionConfig::class.java)!!

    override fun getConfigsFrom(directory: File): List<Pair<File, PartialExtensionConfig>> {
        return directory
            .listFiles { _, name -> name.endsWith(".json") }
            ?.map { file ->
                val partialConfig = partialAdapter.fromJson(file.readText())
                    ?: throw IllegalArgumentException("Wrong format of the config file: ${file.path}")
                Pair(file, partialConfig)
            } ?: emptyList()
    }

    override fun <T : Any> getConfig(clazz: KClass<T>): T {
        TODO("Not yet implemented")
    }

}
