package fr.o80.twitck.lib.internal.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import fr.o80.twitck.lib.api.bean.ChannelName
import fr.o80.twitck.lib.utils.addPrefix

internal class ChannelNameAdapter {

    @ToJson
    fun toJson(channelName: ChannelName): String {
        return channelName.name
    }

    @FromJson
    fun fromJson(name: String): ChannelName {
        return ChannelName(name.addPrefix("#"))
    }

}
