package fr.o80.twitck.example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import fr.o80.twitck.extension.Channel
import fr.o80.twitck.extension.Help
import fr.o80.twitck.extension.points.Points
import fr.o80.twitck.extension.Presence
import fr.o80.twitck.extension.rewards.Rewards
import fr.o80.twitck.extension.RuntimeCommand
import fr.o80.twitck.extension.storage.Storage
import fr.o80.twitck.extension.ViewerPromotion
import fr.o80.twitck.extension.Welcome
import fr.o80.twitck.extension.Whisper
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.twitckBot
import fr.o80.twitck.overlay.StandardOverlay
import java.io.File
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) = Main().main(args)

class Main : CliktCommand() {

    private val oauthToken: String by option(help = "Oauth token of the bot").prompt("Bot's oauth token (oauth-token)")
    private val hostName: String by option(help = "Name of the host channel").prompt("Host's name (host-name)")
    private val botName: String by option(help = "Name of the bot channel").prompt("Bot's name (bot-name)")
    private val clientId: String by option(help = "Twitch Client-ID").prompt("The Client-ID to access Twitch API")
    private val presenceName: String? by option(help = "Name of the host channel")

    override fun run() {
        val hostChannel = "#$hostName"
        val botChannel = "#$botName"

        val bot = configureBot(
            clientId,
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
        clientId: String,
        oauthToken: String,
        hostName: String,
        hostChannel: String,
        botName: String,
        botChannel: String,
        presenceChannel: String?
    ): TwitckBot {
        println("Start...")
        return twitckBot(oauthToken, clientId) {
            install(StandardOverlay) {
            }
            install(Storage) {
                output(File(".storage/"))
            }
            install(Help) {
                channel(hostChannel)
                registerCommand("!nothing", "Cette commande dit qu'elle ne fait rien, du tout.")
                registerCommand("!language", "Ici on fait du Kotlin, best language ever")
                registerCommand("!none")
            }
            install(Points) {
                channel(hostChannel)
                privilegedBadges(Badge.BROADCASTER, Badge.MODERATOR)
                messages(
                    pointsTransferred = "Codes source transferés de #FROM# à #TO#",
                    noPointsEnough = "Les huissiers sont en route vers #FROM#",
                    viewHasNoPoints = "#USER# possède 0 code source",
                    viewHasPoints = "#USER# possède #POINTS# codes source",
                    points = "codes source"
                )
            }
            install(Rewards) {
                channel(hostChannel)
                claim(points = 50, time = 1, unit = TimeUnit.HOURS)
                messages(
                    points = "codes source"
                )
            }
            install(Welcome) {
                channel(hostChannel)
                host(hostName, "Salut $hostName ! Fais comme chez toi hein !?")
                ignore(botName, "lurxx", "anotherttvviewer", "letsdothis_streamers")
                welcomeInterval(2, TimeUnit.HOURS)
                messageForViewer("Compilation de #USER# impossible, trop de bugs.")
                messageForViewer("Décompilation de #USER# en cours...")
                messageForViewer("#USER# télécharge les internets mondiaux, quelqu'un peut lui prêter 1 ou 2 disquettes svp ?")
                messageForViewer("#USER# croit profondément au retour du Pascal...")
                messageForViewer("#USER# vient de finir sa lecture des internets mondiaux.")
                messageForViewer("Quelqu'un peut expliquer à #USER# la différence entre Java et JavaScript ?")
                messageForFollower("Yo #USER# t'es le meilleur des internets mondiaux!")
                messageForFollower("Prière d'accueillir #USER# comme il se doit.")
                messageForFollower("Oh ! Mais ne serait-ce pas le célèbre #USER# qui se joint à nous ?")
                messageForFollower("#USER# !! Mon préféré !")
                messageForFollower("Faites place au prince #USER# !")
            }
            install(ViewerPromotion) {
                channel(hostChannel)
                ignore(hostName, "lurxx", "anotherttvviewer", "letsdothis_streamers")
                promotionInterval(1, TimeUnit.HOURS)
                addMessage("#USER# stream dans la catégorie #GAME#, n'hésitez pas à aller le voir #URL#")
                addMessage("Envie de #GAME# ? n'hésitez pas à aller voir #USER# -> #URL#")
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


