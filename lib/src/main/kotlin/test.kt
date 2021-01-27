import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import fr.o80.twitck.lib.internal.bean.ExtensionConfig
import fr.o80.twitck.lib.internal.bean.OverlayConfig
import fr.o80.twitck.lib.internal.bean.PartialExtensionConfig


fun main() {
    println("Start")

    val moshi = Moshi.Builder().build()
    parseOverlayConfig(moshi)
    println("=========")
    parsePartial(moshi)
}

fun parsePartial(moshi: Moshi) {
    val adapter = moshi.adapter(PartialExtensionConfig::class.java)

    val config = adapter.fromJson(
        """
            {
              "extension": "LwjglOverlay",
              "enabled": true,
              "data": {
                "informationText": "Ceci devrait être ignoré"
              }
            }
        """.trimIndent()
    )!!

    println(config.extension)
    println(config.enabled)
}

private fun parseOverlayConfig(moshi: Moshi) {
    val type = Types.newParameterizedType(
        ExtensionConfig::class.java,
        OverlayConfig::class.java
    )
    val adapter = moshi.adapter<ExtensionConfig<OverlayConfig>>(type)

    val config = adapter.fromJson(
        """
            {
              "extension": "LwjglOverlay",
              "enabled": true,
              "data": {
                "informationText": "Yo les internets c'est Olivier !"
              }
            }
        """.trimIndent()
    )!!

    println(config.extension)
    println(config.enabled)
    println(config.data.informationText)
}