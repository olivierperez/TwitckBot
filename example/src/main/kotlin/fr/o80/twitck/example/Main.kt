package fr.o80.twitck.example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import fr.o80.twitck.extension.Channel
import fr.o80.twitck.extension.Help
import fr.o80.twitck.extension.Presence
import fr.o80.twitck.extension.RuntimeCommand
import fr.o80.twitck.extension.Welcome
import fr.o80.twitck.extension.Whisper
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.twitckBot


fun main(args: Array<String>) = Main().main(args)

class Main : CliktCommand() {

    private val oauthToken: String by option(help = "Oauth token of the bot").prompt("Bot's oauth token (oauth-token)")
    private val hostName: String by option(help = "Name of the host channel").prompt("Host's name (host-name)")
    private val botName: String by option(help = "Name of the bot channel").prompt("Bot's name (bot-name)")
    private val presenceName: String? by option(help = "Name of the host channel")

    override fun run() {
        val hostChannel = "#$hostName"
        val botChannel = "#$botName"

        val bot = configureBot(
            oauthToken,
            hostName,
            hostChannel,
            botName,
            botChannel,
            presenceName
        )

        bot.connectToServer()
        println("Initialized!")

        bot.send(hostChannel, "En position !")
        println("Ready to go!")
    }

    private fun configureBot(
        oauthToken: String,
        hostName: String,
        hostChannel: String,
        botName: String,
        botChannel: String,
        presenceChannel: String?
    ): TwitckBot {
        println("Start...")
        return twitckBot(oauthToken) {
            install(Welcome) {
                channel(hostChannel)
                host(hostName, "Salut $hostName ! Fais comme chez toi hein !?")
                addMessage("Compilation de #USER# impossible, il est plein de bugs.")
                addMessage("Décompilation de #USER# en cours...")
                addMessage("#USER# télécharge les internets mondiaux, il arrive dans 1 ou 2 seconds.")
                addMessage("#USER# croit profondément au retour du Pascal, ne lui dites pas la vérité.")
                addMessage("#USER# vient de finir de lire les internets mondiaux.")
                addMessage("Quelqu'un peut expliquer à #USER# la différence entre Java et JavaScript ?")
            }
            install(Help) {
                channel(hostChannel)
                registerCommand("!nothing", "Cette commande dit qu'elle ne fait rien, du tout.")
                registerCommand("!language", "Ici on fait du Kotlin, best language ever")
                registerCommand("!none")
            }
            install(RuntimeCommand) {
                channel(hostChannel)
                privilegedBadges(Badge.BROADCASTER, Badge.MODERATOR)
            }
            install(Whisper) {
                whisper { bot, whisper ->
                    when {
                        whisper.message.startsWith("!host") -> {
                            val split = whisper.message.split(" ")
                            when (split.size) {
                                2 -> bot.send(botChannel, "/host ${split[1]}")
                                else -> println("Quelque chose ne va pas dans la demande : \"${whisper.message}\"")
                            }
                        }
                        whisper.message == "!unhost" -> {
                            bot.send(botChannel, "/unhost")
                        }
                        whisper.message.startsWith("!shuto") -> {
                            val split = whisper.message.split(" ")
                            when (split.size) {
                                2 -> bot.send(hostChannel, "Envoi d'un shuto à ${split[1]} !")
                                3 -> bot.send(hostChannel, "Envoi d'un shuto ${split[1]} à ${split[2]} !")
                            }
                        }
                        whisper.message.startsWith("!mawashi") -> {
                            val split = whisper.message.split(" ")
                            when (split.size) {
                                2 -> bot.send(hostChannel, "Envoi d'un mawashi geri à ${split[1]} !")
                                3 -> bot.send(hostChannel, "Envoi d'un mawashi geri ${split[1]} à ${split[2]} !")
                            }
                        }
                    }
                }
            }
            install(Channel) {
                channel(botChannel)
                command("!help") { bot, _ ->
                    bot.send(botChannel, "Il n'y a pas encore d'aide")
                }
                join { bot, join ->
                    if (join.login != botName) {
                        bot.send(join.channel, "Salut ${join.login} ! Que fais-tu ici ?")
                        bot.send(hostChannel, "Heu... Il y a ${join.login} qui est venu chez moi, je fais quoi ?")
                    }
                }
            }
            presenceChannel?.let {
                install(Presence) {
                    channel("#$presenceChannel")
                }
            }
        }
    }
}


