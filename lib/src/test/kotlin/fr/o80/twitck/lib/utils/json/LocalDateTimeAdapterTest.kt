package fr.o80.twitck.lib.utils.json

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals


class LocalDateTimeAdapterTest {

    private val jsonAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(LocalDateTimeAdapter())
        .build()
        .adapter(Wrap::class.java)

    @Test
    fun `Should format LocalDateTime with moshi`() {
        val wrapped = Wrap(LocalDateTime.of(2020, 9, 14, 16, 43, 13, 52))
        val json = jsonAdapter.toJson(wrapped)
        assertEquals("{\"time\":\"2020-09-14T16:43:13.000000052\"}", json)
    }

    @Test
    fun `Should parse LocalDateTime with moshi`() {
        val json = "{\"time\":\"2020-09-14T16:43:13.000000052\"}"
        val wrap = jsonAdapter.fromJson(json)!!
        assertEquals(LocalDateTime.of(2020, 9, 14, 16, 43, 13, 52), wrap.time)
    }

}

@JsonClass(generateAdapter = true)
class Wrap(
    val time: LocalDateTime
)
