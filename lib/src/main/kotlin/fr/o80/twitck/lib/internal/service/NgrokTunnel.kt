package fr.o80.twitck.lib.internal.service


import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import java.io.IOException

private const val NGROCK_ADDR: String = "http://127.0.0.1:4040"

class NgrokTunnel(
    val name: String,
    port: Int
) {

    var url: String
        private set


    init {
        close(name)
        val payload = String.format(
            "{"
                    + "\"addr\":\"%d\", "
                    + "\"name\":\"%s\", "
                    + "\"proto\":\"http\", "
                    + "\"bind_tls\":\"false\"" +
                    "}", port, name
        )
        val jsonResponse: HttpResponse<JsonNode> = Unirest.post("$NGROCK_ADDR/api/tunnels")
            .header("accept", "application/json")
            .header("Content-Type", "application/json; charset=utf8")
            .body(payload)
            .asJson()
//        println(jsonResponse.rawBody.reader().readText())
        this.url = jsonResponse.body.getObject().getString("public_url")
    }

    @Throws(IOException::class, UnirestException::class)
    fun close() {
        Unirest.delete("$NGROCK_ADDR/api/tunnels/$name").asString()
    }

    companion object {
        fun close(name: String) {
            Unirest.delete("$NGROCK_ADDR/api/tunnels/$name").asString()
        }
    }
}