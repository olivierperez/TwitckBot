package fr.o80.twitck.example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.int
import fr.o80.twitck.extension.help.DefaultHelpExtension
import fr.o80.twitck.extension.market.Market
import fr.o80.twitck.extension.points.DefaultPointsExtension
import fr.o80.twitck.extension.promotion.ViewerPromotion
import fr.o80.twitck.extension.repeat.Repeat
import fr.o80.twitck.extension.rewards.Rewards
import fr.o80.twitck.extension.sound.DefaultSoundExtension
import fr.o80.twitck.extension.stats.InMemoryStatsExtension
import fr.o80.twitck.extension.storage.InFileStorageExtension
import fr.o80.twitck.extension.welcome.Welcome
import fr.o80.twitck.lib.api.BotFactory
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.overlay.LwjglOverlay
import java.io.File
import java.time.Duration

fun main(args: Array<String>) = Main().main(args)

class Main : CliktCommand() {

    private val oauthToken: String by option(help = "Oauth token of the bot").prompt("Bot's oauth token (oauth-token)")
    private val hostName: String by option(help = "Name of the host channel").prompt("Host's name (host-name)")
    private val botName: String by option(help = "Name of the bot channel").prompt("Bot's name (bot-name)")
    private val presenceName: String? by option(help = "Name of the host channel")
    private val slobsHost: String by option(help = "Streamlabs host").prompt("Streamlabs host")
    private val slobsPort: Int by option(help = "Streamlabs port").int().prompt("Streamlabs port")
    private val slobsToken: String by option(help = "Streamlabs token").prompt("Streamlabs token")

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
        // TODO OPZ Pouvoir configurer les extensions grâce à des fichiers de conf

        // TODO OPZ Acheter une commande pour acheter la couleur du bot dans le chat
        // TODO idée GiftSub !cron pour que le bot nous rappel des choses (genre !timer +9min Va chercher la pizza)
        // TODO idée GiftSub !cron pour que le bot nous rappel des choses (genre !timer !pizza +97min Va chercher la pizza, puis !pizza => "dans 34 minutes")
        return BotFactory(
            configDirectory = File("./configs/"),
            oauthToken = oauthToken,
            hostName = hostName
        )
            .install(LwjglOverlay::installer)
            .install(DefaultSoundExtension::installer)
            .install(InFileStorageExtension::installer)
            .install(DefaultHelpExtension::installer)
            .install(InMemoryStatsExtension::installer)
            .install(DefaultPointsExtension::installer)
            .install(Rewards::installer)
            .install(Market::installer)
            .install(Repeat::installer)
            .install(Welcome::installer)
            .install(ViewerPromotion::installer)
            .create()

        /*return twitckBot(oauthToken, hostName) {
//            --AclExtension--
//            install(CommandAcl) {
//                on("!points_add", Badge.BROADCASTER, Badge.MODERATOR)
//                on("!points_give", Badge.FOUNDER)
//            }
            install(WebSocketRemoteActions) {
                channel(hostChannel)
                slobs(slobsHost, slobsPort, slobsToken)
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
                        sound.playCelebration()
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
                command("!screen") { _, _, extensionProvider ->
                    extensionProvider.first(SoundExtension::class).play("screen")
                }
                command("!yata") { _, _, extensionProvider ->
                    extensionProvider.first(SoundExtension::class).play("yata")
                    extensionProvider.showImage("image/vahine.gif", "Yata Yata Yata")
                }
                command("!youpi") { _, _, extensionProvider ->
                    extensionProvider.first(SoundExtension::class).play("youpi")
                    extensionProvider.showImage("image/vahine.gif", "Youpi !")
                }
                command("!gogol") { _, _, extensionProvider ->
                    extensionProvider.first(SoundExtension::class).play("gogol")
                }
            }
            install(Channel) {
                channel(botChannel)
                command("!help") { messenger, _, _ ->
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
        }*/
    }
}

private fun ExtensionProvider.showImage(imagePath: String, text: String) {
    val path = javaClass.classLoader.getResourceAsStream(imagePath)
        ?: throw IllegalArgumentException("Failed to load image for resources: $imagePath")

    this.first(OverlayExtension::class)
        .showImage(path, text, Duration.ofSeconds(5))
}
