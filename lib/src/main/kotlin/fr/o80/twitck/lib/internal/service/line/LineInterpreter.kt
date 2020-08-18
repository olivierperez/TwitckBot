package fr.o80.twitck.lib.internal.service.line

internal class LineInterpreter(
    private vararg val handlers: LineHandler
) {
    fun handle(line: String) {
        handlers.forEach { handler -> handler.handle(line) }
    }
}
