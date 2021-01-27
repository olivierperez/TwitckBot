package fr.o80.twitck.lib.internal.bean

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ExtensionConfig<T>(
    val extension: String,
    val enabled: Boolean,
    val data: T
)
@JsonClass(generateAdapter = true)
class PartialExtensionConfig(
    val extension: String,
    val enabled: Boolean
)

@JsonClass(generateAdapter = true)
class OverlayConfig(
    val informationText: String
)
