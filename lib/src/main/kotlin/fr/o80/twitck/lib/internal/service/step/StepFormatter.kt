package fr.o80.twitck.lib.internal.service.step

import fr.o80.twitck.lib.api.service.step.StepParam

class StepFormatter {

    private val indexedParamRegex = "#PARAM-(\\d+)#".toRegex()

    fun format(input: String, param: StepParam): String =
        input
            .formatFor(param.viewerName)
            .formatWith(param.params)

    private fun String.formatFor(viewerName: String): String =
        this.replace("#USER#", viewerName)

    private fun String.formatWith(params: List<String>): String =
        this.replaceIndexedParams(params)
            .replaceFullLengthParams(params)

    private fun String.replaceIndexedParams(params: List<String>) =
        indexedParamRegex.findAll(this)
            .fold(this) { acc, match ->
                val paramId = match.groupValues[1].toInt()
                acc.replace("#PARAM-$paramId#", params[paramId])
            }

    private fun String.replaceFullLengthParams(params: List<String>): String =
        this.replace("#PARAMS#", params.joinToString(" "))

}
