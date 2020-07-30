package fr.o80.twitck.lib.service.line

class LineInterpreter(
    private vararg val handlers: LineHandler
) {
    fun handle(line: String) {
        handlers.forEach { handler -> handler.handle(line) }
    }
}
