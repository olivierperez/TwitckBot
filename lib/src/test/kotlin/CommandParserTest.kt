import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.api.service.CommandParser
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandParserTest {

    private val messagesCases: Map<String, Command?> = mapOf(
        // Should return a command
        "!my_command" to Command("!my_command", emptyList()),
        "!my_command param_1" to Command("!my_command", listOf("param_1")),
        "!my_command param_1 param_2" to Command("!my_command", listOf("param_1", "param_2")),

        "!" to null,

        "" to null,
        "my_command" to null,
        "my_command param_1" to null,
        "my_command param_1 param_2" to null,

        " " to null,
        " my_command" to null,
        " my_command param_1" to null,
        " my_command param_1 param_2" to null,

        " !" to null,
        " !my_command" to null,
        " !my_command param_1" to null,
        " !my_command param_1 param_2" to null,

        "my_command!" to null,
        "my_command!my_command" to null,
        "my_command!my_command param_1" to null,
        "my_command!my_command param_1 param_2" to null,

        "my_command !" to null,
        "my_command !my_command" to null,
        "my_command !my_command param_1" to null,
        "my_command !my_command param_1 param_2" to null
    )

    @Test
    fun testAllMessages() {
        messagesCases.forEach {
            val result = CommandParser().parse(it.key)

            assertEquals(it.value, result, "Input ${it.key}")
        }
    }

}