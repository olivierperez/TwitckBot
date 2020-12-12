package fr.o80.twitck.lib.internal.service.line

internal class LineInterpreters(
    private vararg val interpreters: LineInterpreter
) {
    fun interpretLine(line: String) {
        interpreters.forEach { handler -> handler.handle(line) }
    }
}
