package fr.o80.twitck.extension.ngrok

import fr.o80.twitck.lib.api.service.log.Logger
import java.util.concurrent.TimeUnit

class NgrokProcess(
    private val pathToNgrok: String,
    private val token: String,
    private val logger: Logger
) {

    fun launch() {
        var process: Process? = null

        try {
            logger.info("Starting ngrok...")
            process = ProcessBuilder(pathToNgrok, "http", "80", "-authtoken", token)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            if (process.waitFor(1, TimeUnit.SECONDS)) {
                error("Ngrok early termination")
            }
            atShutdown {
                process.waitFor(1, TimeUnit.SECONDS)
                logger.info("Destroying ngrok process")
                process.destroy()
            }
        } catch (e: Exception) {
            // TODO OPZ Do not log if we throw
            val error = process?.errorStream?.bufferedReader()?.readText()?.let { "Error : $it" }
            val output = process?.inputStream?.bufferedReader()?.readText()?.let { "Output : $it" }
            val messages = listOfNotNull("Ngrok start failure: ${e.message}", error, output)
            logger.error(messages.joinToString("\n\n"), e)
            throw e
        }
    }

    private fun atShutdown(block: () -> Unit) {
        Runtime.getRuntime().addShutdownHook(Thread { block() })
    }

}
