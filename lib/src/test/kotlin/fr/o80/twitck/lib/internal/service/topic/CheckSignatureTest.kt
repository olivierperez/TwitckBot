package fr.o80.twitck.lib.internal.service.topic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class CheckSignatureTest {

    private val secret = "a secret"

    private val checkSignature = CheckSignature(secret)

    @Test
    fun buildSignature() {
        assertEquals(
            "sha256=e36a6bc6df5ebc91930015893704b43ae57e60f0cfc6f95131e85996f364c6ab",
            "a message".toSignature(secret)
        )
        assertEquals(
            "sha256=4990d8bd3c5e7acb0e3ab704275d481914f1aae2165aee9a7cc8d885bd920e3e",
            "another message".toSignature(secret)
        )
    }

    @Test
    fun checkValidSignature() {
        val signatureResult = checkSignature(
            mapOf("X-Hub-Signature" to listOf("sha256=e36a6bc6df5ebc91930015893704b43ae57e60f0cfc6f95131e85996f364c6ab")),
            "a message"
        )

        assertEquals(SignatureResult.Valid, signatureResult)
    }

    @Test
    fun checkMissingSignature() {
        val signatureResult = checkSignature(
            mapOf(),
            "a message"
        )

        assertTrue(signatureResult is SignatureResult.Failed)
    }

    @Test
    fun checkWrongSignature() {
        val signatureResult = checkSignature(
            mapOf("X-Hub-Signature" to listOf("sha256=wrong")),
            "a message"
        )

        assertTrue(signatureResult is SignatureResult.Invalid)
    }

}