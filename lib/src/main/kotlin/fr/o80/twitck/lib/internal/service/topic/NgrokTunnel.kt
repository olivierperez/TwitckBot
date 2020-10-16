package fr.o80.twitck.lib.internal.service.topic


import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import org.json.JSONObject

private const val NGROCK_ADDR: String = "http://127.0.0.1:4040"

class NgrokTunnel(
    private val name: String,
    private val port: Int
) {

    fun getOrOpenTunnel(): String = getCurrentTunnel() ?: openTunnel()

    private fun getCurrentTunnel(): String? {
        val jsonResponse = Unirest.get("$NGROCK_ADDR/api/tunnels")
            .header("accept", "application/json")
            .header("Content-Type", "application/json; charset=utf8")
            .asJson()

        return jsonResponse.body.getObject()
            .getJSONArray("tunnels")
            .filterIsInstance(JSONObject::class.java)
            .firstOrNull { tunnel -> tunnel.getString("name") == name }
            ?.getString("public_url")
    }

    private fun openTunnel(): String {
        val payload = """{"addr":"$port", "name":"$name", "proto":"http", "bind_tls":"true"}"""
        val jsonResponse: HttpResponse<JsonNode> = Unirest.post("$NGROCK_ADDR/api/tunnels")
            .header("accept", "application/json")
            .header("Content-Type", "application/json; charset=utf8")
            .body(payload)
            .asJson()
//        println(jsonResponse.rawBody.reader().readText())
        return jsonResponse.body.getObject().getString("public_url")
    }

    fun close() {
        Unirest.delete("$NGROCK_ADDR/api/tunnels/$name").asString()
    }

    companion object {
        fun close(name: String) {
            Unirest.delete("$NGROCK_ADDR/api/tunnels/$name").asString()
        }
    }
}