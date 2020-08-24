import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.service.CommandParser
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandParserTest {

    private class TwitckBotMock: TwitckBot {
        override fun connectToServer() { }

        override fun join(channel: String) { }

        override fun sendLine(line: String) { }

        override fun send(channel: String, message: String) { }

        override fun onMessage(channel: String, sender: String, login: String, hostname: String, message: String) { }

        override fun onJoin(channel: String, sender: String, login: String, hostname: String) { }
    }

    private fun createMessageEvent(login: String, message: String, badges: List<Badge>): MessageEvent {
        return MessageEvent(TwitckBotMock(), "channel", login, "userId", message, badges)
    }

    val messagesCases: Map<MessageEvent, Command?> = mapOf(
        // Should return a command
        createMessageEvent("", "!my_command", emptyList()) to Command("", emptyList(), "!my_command", emptyList()),
        createMessageEvent("", "!my_command param_1", emptyList()) to Command("", emptyList(), "!my_command", listOf("param_1")),
        createMessageEvent("", "!my_command param_1 param_2", emptyList()) to Command("", emptyList(), "!my_command", listOf("param_1", "param_2")),
        // Should return null
        createMessageEvent("", "!", emptyList()) to null,
        //
        createMessageEvent("", "", emptyList()) to null,
        createMessageEvent("", "my_command", emptyList()) to null,
        createMessageEvent("", "my_command param_1", emptyList()) to null,
        createMessageEvent("", "my_command param_1 param_2", emptyList()) to null,
        //
        createMessageEvent("", " ", emptyList()) to null,
        createMessageEvent("", " my_command", emptyList()) to null,
        createMessageEvent("", " my_command param_1", emptyList()) to null,
        createMessageEvent("", " my_command param_1 param_2", emptyList()) to null,
        //
        createMessageEvent("", " !", emptyList()) to null,
        createMessageEvent("", " !my_command", emptyList()) to null,
        createMessageEvent("", " !my_command param_1", emptyList()) to null,
        createMessageEvent("", " !my_command param_1 param_2", emptyList()) to null,
        //
        createMessageEvent("", "my_command!", emptyList()) to null,
        createMessageEvent("", "my_command!my_command", emptyList()) to null,
        createMessageEvent("", "my_command!my_command param_1", emptyList()) to null,
        createMessageEvent("", "my_command!my_command param_1 param_2", emptyList()) to null,
        //
        createMessageEvent("", "my_command !", emptyList()) to null,
        createMessageEvent("", "my_command !my_command", emptyList()) to null,
        createMessageEvent("", "my_command !my_command param_1", emptyList()) to null,
        createMessageEvent("", "my_command !my_command param_1 param_2", emptyList()) to null
    )

    val badgesCases: Map<MessageEvent, Command?> = mapOf(
        createMessageEvent("", "!my_command", emptyList()) to Command("", emptyList(), "!my_command", emptyList()),
        createMessageEvent("", "!my_command", listOf(Badge.ADMIN)) to Command("", listOf(Badge.ADMIN), "!my_command", emptyList()),
        createMessageEvent("", "!my_command", listOf(Badge.ADMIN, Badge.BROADCASTER)) to Command("", listOf(Badge.ADMIN, Badge.BROADCASTER), "!my_command", emptyList()),
        createMessageEvent("", "!my_command", listOf(Badge.ADMIN, Badge.ADMIN)) to Command("", listOf(Badge.ADMIN, Badge.ADMIN), "!my_command", emptyList())
    )

    val loginsCases: Map<MessageEvent, Command?> = mapOf(
        createMessageEvent("", "!my_command", emptyList()) to Command("", emptyList(), "!my_command", emptyList()),
        createMessageEvent("login", "!my_command", emptyList()) to Command("login", emptyList(), "!my_command", emptyList()),
        createMessageEvent("LOGIN", "!my_command", emptyList()) to Command("LOGIN", emptyList(), "!my_command", emptyList()),
        createMessageEvent("login with space", "!my_command", emptyList()) to Command("login with space", emptyList(), "!my_command", emptyList()),
        createMessageEvent("l0g1n;)?!@#é'", "!my_command", emptyList()) to Command("l0g1n;)?!@#é'", emptyList(), "!my_command", emptyList())
    )

    @Test
    fun testAllMessages() {
        messagesCases.forEach {
            val result = CommandParser().parse(it.key)

            assertEquals(it.value, result, "Input ${it.key}")
        }
    }

    @Test
    fun testAllBadgesCases() {
        badgesCases.forEach {
            val result = CommandParser().parse(it.key)

            assertEquals(it.value, result, "Input ${it.key}")
        }
    }

    @Test
    fun testAllLoginsCases() {
        loginsCases.forEach {
            val result = CommandParser().parse(it.key)

            assertEquals(it.value, result, "Input ${it.key}")
        }
    }

}