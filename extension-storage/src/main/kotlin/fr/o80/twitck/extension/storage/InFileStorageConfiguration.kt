package fr.o80.twitck.extension.storage

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class InFileStorageConfiguration(
    val storageDirectory: String
)
