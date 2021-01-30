package fr.o80.twitck.lib.internal.bean

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ExtensionConfig<T>(
    val extension: String,
    val enabled: Boolean,
    val data: T
)
