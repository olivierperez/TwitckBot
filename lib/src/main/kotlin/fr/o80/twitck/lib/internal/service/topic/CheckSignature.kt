package fr.o80.twitck.lib.internal.service.topic

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class CheckSignature(
    private val secret: String
) {

    operator fun invoke(headers: Map<String, List<String>>, body: String): SignatureResult {
        val signature = headers["X-Hub-Signature"]?.firstOrNull()
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
    class Invalid(val signature: String, val computedSignature: String, val body: String) :
        SignatureResult()

    class Failed(val message: String) : SignatureResult()
}
