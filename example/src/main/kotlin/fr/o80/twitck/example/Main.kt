package fr.o80.twitck.example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import fr.o80.twitck.example.market.CodeReviewProduct
import fr.o80.twitck.example.market.CommandProduct
import fr.o80.twitck.example.market.CompareLanguageProduct
import fr.o80.twitck.example.market.KotlinProduct
import fr.o80.twitck.extension.Channel
import fr.o80.twitck.extension.Help
import fr.o80.twitck.extension.Presence
import fr.o80.twitck.extension.RuntimeCommand
import fr.o80.twitck.extension.ViewerPromotion
import fr.o80.twitck.extension.Welcome
import fr.o80.twitck.extension.Whisper
import fr.o80.twitck.extension.market.Market
import fr.o80.twitck.extension.points.Points
import fr.o80.twitck.extension.repeat.Repeat
import fr.o80.twitck.extension.rewards.Rewards
import fr.o80.twitck.extension.storage.Storage
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.twitckBot
import fr.o80.twitck.overlay.StandardOverlay
import fr.o80.twitck.poll.Poll
import java.io.File
import java.time.Duration

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
            oauthToken = oauthToken,
            hostName = hostName,
            hostChannel = hostChannel,
            botName = botName,
            botChannel = botChannel,
            presenceChannel = presenceName
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
            install(StandardOverlay) {
            }
            install(Storage) {
                output(File(".storage/"))
            }
            install(Help) {
                channel(hostChannel)
                registerCommand("!nothing", "Cette commande dit qu'elle ne fait rien, du tout.")
                registerCommand("!language", "Ici on fait du Kotlin, best language ever")
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
                claim(points = 15, time = Duration.ofMinutes(20))
                rewardTalkativeViewers(points = 5, time = Duration.ofMinutes(10))
                messages(
                    points = "codes source",
                    viewerJustClaimed = "#USER# vient de collecter #NEW_POINTS# codes source et en possède donc #OWNED_POINTS#"
                )
            }
            install(Market) {
                channel(hostChannel)
                product(CommandProduct)
                product(CodeReviewProduct)
                product(KotlinProduct)
                product(CompareLanguageProduct)
                // TODO idée CamouilleLaFripouille 500 codes source => assistance UML (15 min)
            }
            install(Repeat) {
                channel(hostChannel)
                interval(Duration.ofMinutes(5))
                remind("Retrouvez mon code source sur https://github.com/olivierperez/TwitckBot")
                remind("Olivier tweet peu, mais bien https://twitter.com/olivierperez")
                remind("Quelqu'un veut qu'on monte un discord ?")
                remind("Olivier partage quelques bouts de code sur https://github.com/olivierperez")
                remind("Vous savez qu'on streame du Game Dev de temps en temps ?")
                remind("Vous faites de l'Android ? nous aussi, et on le streame parfois")
                remind("Le refactoring Kata c'est bon pour la santé, du coup on le pratique ici de temps en temps")
            }
            install(Welcome) {
                channel(hostChannel)
                host(hostName, "Salut $hostName ! Fais comme chez toi hein !?")
                ignore(botName, "lurxx", "anotherttvviewer", "letsdothis_streamers")
                welcomeInterval(Duration.ofHours(2))
                messageForViewer("Compilation de #USER# impossible, trop de bugs.")
                messageForViewer("Décompilation de #USER# en cours...")
                messageForViewer("#USER# télécharge les internets mondiaux, quelqu'un peut lui prêter 1 ou 2 disquettes svp ?")
                messageForViewer("#USER# croit profondément au retour du Pascal...")
                messageForViewer("#USER# vient de finir sa lecture des internets mondiaux.")
                messageForViewer("Quelqu'un peut expliquer à #USER# la différence entre Java et JavaScript ?")
                messageForViewer("#USER# a presque DL tout Wikipedia")
                messageForFollower("Yo #USER# t'es le meilleur des internets mondiaux!")
                messageForFollower("Prière d'accueillir #USER# comme il se doit.")
                messageForFollower("Oh ! Mais ne serait-ce pas le célèbre #USER# qui se joint à nous ?")
                messageForFollower("#USER# !! Mon préféré !")
                messageForFollower("Faites place au prince #USER# !")
            }
            install(ViewerPromotion) {
                channel(hostChannel)
                ignore(hostName, "lurxx", "anotherttvviewer", "letsdothis_streamers")
                promotionInterval(Duration.ofHours(1))
                addMessage("#USER# stream dans la catégorie #GAME#, n'hésitez pas à aller le voir #URL#")
                addMessage("Envie de #GAME# ? n'hésitez pas à aller voir #USER# -> #URL#")
            }
            install(Poll) {
                channel(hostChannel)
                privilegedBadges(Badge.BROADCASTER, Badge.MODERATOR)
                pointsToEarn(15)
                messages(
                    errorCreationPollUsage = "Pour créer un sondage : \"!poll <durée> <question>\"",
                    errorDurationIsMissing = "Il faut choisir la durée du sondage !",
                    newPoll = "Nouveau sondage : #TITLE# Utilisez !vote pour répondre",
                    pollHasJustFinished = "Sondage terminé. #TITLE# #RESULTS#",
                    currentPollResult = "Sondage en cours... #TITLE# #RESULTS#",
                    pollHasNoVotes = "Personne n'a répondu à la question #TITLE#"
                )
            }
            install(RuntimeCommand) {
                channel(hostChannel)
                privilegedBadges(Badge.BROADCASTER, Badge.MODERATOR)
            }
            install(Whisper) {
                whisper { messenger, whisper ->
                    when {
                        whisper.message.startsWith("!host") -> {
                            val split = whisper.message.split(" ")
                            when (split.size) {
                                2 -> messenger.sendImmediately(botChannel, "/host ${split[1]}")
                                else -> println("Quelque chose ne va pas dans la demande : \"${whisper.message}\"")
                            }
                        }
                        whisper.message == "!unhost" -> {
                            messenger.sendImmediately(botChannel, "/unhost")
                        }
                        whisper.message.startsWith("!shuto") -> {
                            val split = whisper.message.split(" ")
                            when (split.size) {
                                2 -> messenger.sendImmediately(
                                    hostChannel,
                                    "Envoi d'un shuto à ${split[1]} !"
                                )
                                3 -> messenger.sendImmediately(
                                    hostChannel,
                                    "Envoi d'un shuto ${split[1]} à ${split[2]} !"
                                )
                            }
                        }
                        whisper.message.startsWith("!mawashi") -> {
                            val split = whisper.message.split(" ")
                            when (split.size) {
                                2 -> messenger.sendImmediately(
                                    hostChannel,
                                    "Envoi d'un mawashi geri à ${split[1]} !"
                                )
                                3 -> messenger.sendImmediately(
                                    hostChannel,
                                    "Envoi d'un mawashi geri ${split[1]} à ${split[2]} !"
                                )
                            }
                        }
                    }
                }
            }
            install(Channel) {
                channel(botChannel)
                command("!help") { messenger, _ ->
                    messenger.sendImmediately(botChannel, "Il n'y a pas encore d'aide")
                }
                join { messenger, join ->
                    if (!join.login.equals(botName, true)) {
                        messenger.sendImmediately(
                            join.channel,
                            "Salut ${join.login} ! Que fais-tu ici ?"
                        )
                        messenger.sendImmediately(
                            hostChannel,
                            "Heu... Il y a ${join.login} qui est venu chez moi, je fais quoi ?"
                        )
                    }
                }
                follow { messenger, followEvent ->
                    val newFollowers = followEvent.followers.data

                    if (newFollowers.size == 1) {
                        messenger.sendImmediately(hostChannel, "Merci ${newFollowers[0].fromName} pour ton follow!", CoolDown(Duration.ofHours(1)))
                    } else {
                        val names = newFollowers.joinToString(" ") { it.fromName }
                        messenger.sendImmediately(hostChannel, "Merci pour vos follows $names", CoolDown(Duration.ofHours(1)))
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


