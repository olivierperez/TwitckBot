package fr.o80.twitck.lib.internal.service.topic

import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.utils.Do
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext

fun Route.protectedBySignature(logger: Logger, secret: String, route: Route.() -> Unit): Route {
    val checkSignature = CheckSignature(secret)

    return createChild(object : RouteSelector(1.0) {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) =
            RouteSelectorEvaluation.Constant
    }).apply {
        intercept(ApplicationCallPipeline.Features) {
            Do exhaustive when (val result = checkSignature(call)) {
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
