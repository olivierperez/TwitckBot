package fr.o80.twitck.lib.internal.service.topic

import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.utils.Do
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.*

fun Route.protectedBySignature(logger: Logger, secret: String, route: Route.() -> Unit): Route {
    val checkSignature = CheckSignature(secret)

    return createChild(object : RouteSelector(1.0) {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) =
            RouteSelectorEvaluation.Constant
    }).apply {
        intercept(ApplicationCallPipeline.Features) {
            val headers = call.request.headers.toMap()
            val body = call.receiveText()
            Do exhaustive when (val result = checkSignature(headers, body)) {
                SignatureResult.Valid -> {
                    logger.debug("Signature is valid!")
                    proceed()
                }
                is SignatureResult.Invalid -> {
                    logger.warn("Failed to check signature \"${result.signature}\" / \"${result.computedSignature}\"! but for now we authorize unchecked signature...\n----\n${result.body}\n----")
                    finish()
                }
                is SignatureResult.Failed -> {
                    logger.error("Something gone wrong while checking signature: ${result.message}")
                    finish()
                }
            }
        }
    }.also {
        route()
    }
}
