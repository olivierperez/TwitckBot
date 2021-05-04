package fr.o80.twitck.lib.internal.service.step

import fr.o80.twitck.lib.api.service.step.StepParams

class StepFormatter {

    private val indexedParamRegex = "#PARAM-(\\d+)#".toRegex()

    fun format(input: String, params: StepParams): String =
        input
            .formatFor(params.viewerName)
            .formatWith(params.params)
            .formatWithBits(params.bits)
            .formatWithMessage(params.message)

    private fun String.formatFor(viewerName: String): String =
        this.replace("#USER#", viewerName)

    private fun String.formatWith(params: List<String>): String =
        this.replaceIndexedParams(params)
            .replaceFullLengthParams(params)

    private fun String.formatWithBits(bits: Int?): String {
        return bits?.let { this.replace("#BITS#", bits.toString()) } ?: this
    }

    private fun String.formatWithMessage(message: String?) : String {
        return message?.let { this.replace("#MESSAGE#", message) } ?: this
    }

    private fun String.replaceIndexedParams(params: List<String>) =
        indexedParamRegex.findAll(this)
            .fold(this) { acc, match ->
                val paramId = match.groupValues[1].toInt()
                acc.replace("#PARAM-$paramId#", params[paramId])
            }

    private fun String.replaceFullLengthParams(params: List<String>): String =
        this.replace("#PARAMS#", params.joinToString(" "))

}
