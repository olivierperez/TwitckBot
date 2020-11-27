package fr.o80.twitck.example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import fr.o80.twitck.example.market.CodeReviewProduct
import fr.o80.twitck.example.market.CommandProduct
import fr.o80.twitck.example.market.CompareLanguageProduct
import fr.o80.twitck.example.market.KotlinProduct
import fr.o80.twitck.extension.Presence
import fr.o80.twitck.extension.channel.Channel
import fr.o80.twitck.extension.help.Help
import fr.o80.twitck.extension.market.Market
import fr.o80.twitck.extension.points.Points
import fr.o80.twitck.extension.promotion.ViewerPromotion
import fr.o80.twitck.extension.repeat.Repeat
import fr.o80.twitck.extension.rewards.Rewards
import fr.o80.twitck.extension.runtimecommand.RuntimeCommand
import fr.o80.twitck.extension.sound.DefaultSoundExtension
import fr.o80.twitck.extension.stats.StatsExtension
import fr.o80.twitck.extension.storage.Storage
import fr.o80.twitck.extension.welcome.Welcome
import fr.o80.twitck.extension.whisper.Whisper
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.bean.Importance
import fr.o80.twitck.lib.api.extension.SoundExtension
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
        // TODO CamouilleLaFrippouille -> répondre à quelques questions, genre : pourquoi tu t'appelles comme ça ?
        return twitckBot(oauthToken, hostName) {
//            --AclExtension--
//            install(CommandAcl) {
//                on("!points_add", Badge.BROADCASTER, Badge.MODERATOR)
//                on("!points_give", Badge.FOUNDER)
//            }
            install(StandardOverlay) {
            }
            install(DefaultSoundExtension) {
            }
            install(Storage) {
                output(File(".storage/"))
            }
            install(Help) {
                channel(hostChannel)
                registerCommand("!nothing", "Cette commande dit qu'elle ne fait rien, du tout.")
                registerCommand("!language", "Ici on fait du Kotlin, best language ever")
            }
            install(StatsExtension) {
                channel(hostChannel)
            }
            install(Points) {
                channel(hostChannel)
                privilegedBadges(Badge.BROADCASTER, Badge.MODERATOR)
                messages(
                    destinationViewerDoesNotExist = "Le destinataire n'existe pas",
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
                rewardTalkativeViewers(points = 5, time = Duration.ofMinutes(5))
                messages(
                    points = "codes source",
                    viewerJustClaimed = "#USER# vient de collecter #NEW_POINTS# codes source et en possède donc #OWNED_POINTS#"
                )
            }
            install(Market) {
                channel(hostChannel)
                messages(
                    couldNotGetProductPrice = "Impossible de calculer le prix pour l'achat demandé !",
                    productNotFound = "le produit n'existe pas",
                    usage = "Usage de !buy => !buy <produit> <paramètres>",
                    weHaveThisProducts = "Voilà tout ce que j'ai sur l'étagère : #PRODUCTS#",
                    youDontHaveEnoughPoints = "@#USER# tu n'as pas assez de codes source pour cet achat !",
                    yourPurchaseIsPending = "@#USER# ton achat est en attente de validation"
                )
                product(CommandProduct)
                product(CodeReviewProduct)
                product(KotlinProduct)
                product(CompareLanguageProduct)
                // TODO idée CamouilleLaFripouille 500 codes source => assistance UML (15 min)
                // TODO idée GiftSub !cron pour que le bot nous rappel des choses (genre !timer +9min Va chercher la pizza)
                // TODO idée GiftSub !cron pour que le bot nous rappel des choses (genre !timer !pizza +97min Va chercher la pizza, puis !pizza => "dans 34 minutes")
            }
            install(Repeat) {
                channel(hostChannel)
                interval(Duration.ofMinutes(15))
                remind("Retrouvez mon code source sur https://github.com/olivierperez/TwitckBot")
                remind("Olivier tweet peu, mais bien https://twitter.com/olivierperez")
                remind("On se retrouve sur discord en dehors des streams ? https://discord.gg/S4HxU2YfaT")
                remind("Olivier partage quelques bouts de code sur https://github.com/olivierperez")
                remind("Vous savez qu'on streame du Game Dev de temps en temps ?")
                remind("Vous faites de l'Android ? nous aussi, et ça se retrouve sur Youtube https://youtu.be/ig-_10msUUE")
                remind("Le refactoring Kata c'est bon pour la santé, du coup on le pratique ici de temps en temps")
            }
            install(Welcome) {
                channel(hostChannel)
                host(hostName, "Salut $hostName ! Fais comme chez toi hein !?")
                ignore(botName, "lurxx", "anotherttvviewer", "letsdothis_streamers")
                reactTo(joins = false, messages = true, commands = true, raids = true)
                welcomeInterval(Duration.ofHours(2))
                messageForViewer("Compilation de #USER# impossible, trop de bugs.")
                messageForViewer("Décompilation de #USER# en cours...")
                messageForViewer("#USER# télécharge les internets mondiaux, quelqu'un peut lui prêter 1 ou 2 disquettes svp ?")
                messageForViewer("#USER# croit profondément au retour du Pascal...")
                messageForViewer("#USER# vient de finir sa lecture des internets mondiaux.")
                messageForViewer("Quelqu'un peut expliquer à #USER# la différence entre Java et JavaScript ?")
                messageForViewer("#USER# a presque DL tout Wikipedia")
                messageForFollower("Yo #USER# la meilleure personne des internets mondiaux!")
                messageForFollower("Prière d'accueillir #USER# comme il se doit.")
                messageForFollower("Oh ! Mais ne serait-ce pas notre célèbre #USER# qui se joint à nous ?")
                messageForFollower("OMG ! mais c'est #USER# !!")
                messageForFollower("Faites place à sa majesté #USER# !")
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
                channel(hostChannel)
                follow { messenger, newFollowers, extensionProvider ->
                    if (newFollowers.size == 1) {
                        messenger.sendImmediately(
                            hostChannel,
                            "Merci ${newFollowers[0].fromName} pour ton follow !",
                            CoolDown(Duration.ofHours(1))
                        )
                    } else {
                        val names = newFollowers.joinToString(" ") { it.fromName }
                        messenger.sendImmediately(
                            hostChannel,
                            "Merci pour vos follows $names",
                            CoolDown(Duration.ofHours(1))
                        )
                    }
                    extensionProvider.forEach(SoundExtension::class) { sound ->
                        sound.playYoupi()
                    }
                }
                newSubscriptions { messenger, events ->
                    events.forEach { subscription ->
                        if (subscription.isGift) {
                            messenger.sendWhenAvailable(
                                hostChannel,
                                "Merci ${subscription.gifterName} pour le cadeau à ${subscription.userName} !",
                                Importance.HIGH
                            )
                        } else {
                            messenger.sendWhenAvailable(
                                hostChannel,
                                "Merci ${subscription.userName} pour le sub!",
                                Importance.HIGH
                            )
                        }
                    }
                }
                notificationSubscriptions { messenger, events ->
                    events.forEach { notification ->
                        println("T'as un message de ${notification.userName} -> ${notification.message}")
                    }
                    messenger.sendImmediately(
                        hostChannel,
                        "DEBUG Hé $hostName, tu as vu le message de ${events.joinToString(", ") { it.userName }}"
                    )
                }
                unknownTypeSubscriptions { messenger, eventType, events ->
                    messenger.sendImmediately(
                        hostChannel,
                        "DEBUG J'ai reçu des notification bizarres, regarde donc les logs !"
                    )
                    println(">>Notifications bizarres<<\n\n")
                    println("---${eventType}---\n$events\n---")
                    events.forEach { println("$it") }
                    println("\n\n>>/Notifications bizarres<<")
                }
            }
            install(Channel) {
                channel(botChannel)
                command("!help") { messenger, _ ->
                    messenger.sendImmediately(botChannel, "Il n'y a pas encore d'aide")
                }
                join { messenger, join ->
                    if (!join.viewer.login.equals(botName, true)) {
                        messenger.sendImmediately(
                            join.channel,
                            "Salut ${join.viewer.displayName} ! Que fais-tu ici ?"
                        )
                        messenger.sendImmediately(
                            hostChannel,
                            "Heu... Il y a ${join.viewer.displayName} qui est venu chez moi, je fais quoi ?"
                        )
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


