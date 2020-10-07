package fr.o80.twitck.lib.internal.service.topic

import io.ktor.application.ApplicationCall
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

// TODO OPZ Des tests unitaires peut-être ?

class CheckSignature(
    private val secret: String
) {

    operator fun invoke(call: ApplicationCall, body: String): SignatureResult {
        val signature = call.request.headers["X-Hub-Signature"]
            ?: return SignatureResult.Failed("There are no X-Hub-Signature in the headers")

        val computedSignature = body.toSignature(secret)
        if (computedSignature != signature) {
            return SignatureResult.Invalid(signature, computedSignature, body)
        }

        return SignatureResult.Valid
    }

}

fun String.toSignature(secret: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    val signingKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
    mac.init(signingKey)
    val rawSignature = mac.doFinal(this.toByteArray())
    return "sha256=" + rawSignature.fold("") { str, it -> str + "%02x".format(it) }
}

sealed class SignatureResult {
    object Valid : SignatureResult()
    class Invalid(val signature: String, val computedSignature: String, val body: String) : SignatureResult()
    class Failed(val message: String) : SignatureResult()
}
