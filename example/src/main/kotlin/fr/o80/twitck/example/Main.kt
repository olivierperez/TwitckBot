package fr.o80.twitck.example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.int
import fr.o80.twitck.extension.actions.WebSocketRemoteActions
import fr.o80.twitck.extension.help.DefaultHelpExtension
import fr.o80.twitck.extension.market.Market
import fr.o80.twitck.extension.points.DefaultPointsExtension
import fr.o80.twitck.extension.promotion.ViewerPromotion
import fr.o80.twitck.extension.repeat.Repeat
import fr.o80.twitck.extension.rewards.Rewards
import fr.o80.twitck.extension.runtimecommand.RuntimeCommand
import fr.o80.twitck.extension.sound.DefaultSoundExtension
import fr.o80.twitck.extension.stats.InMemoryStatsExtension
import fr.o80.twitck.extension.storage.InFileStorageExtension
import fr.o80.twitck.extension.welcome.Welcome
import fr.o80.twitck.lib.api.BotFactory
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.overlay.LwjglOverlay
import fr.o80.twitck.poll.Poll
import java.io.File
import java.time.Duration

fun main(args: Array<String>) = Main().main(args)

class Main : CliktCommand() {

    private val oauthToken: String by option(help = "Oauth token of the bot").prompt("Bot's oauth token (oauth-token)")
    private val hostName: String by option(help = "Name of the host channel").prompt("Host's name (host-name)")

    override fun run() {
        val bot = configureBot(
            oauthToken = oauthToken,
            hostName = hostName
        )

        bot.connectToServer()
        banner()
    }

    private fun configureBot(
        oauthToken: String,
        hostName: String
    ): TwitckBot {
        println("Starting...")
        // TODO OPZ Pouvoir configurer les extensions grâce à des fichiers de conf

        // TODO OPZ Extension ACL
        // TODO OPZ Extension Whisper
        // TODO OPZ Acheter une commande pour acheter la couleur du bot dans le chat
        // TODO idée GiftSub !cron pour que le bot nous rappel des choses (genre !timer +9min Va chercher la pizza)
        // TODO idée GiftSub !cron pour que le bot nous rappel des choses (genre !timer !pizza +97min Va chercher la pizza, puis !pizza => "dans 34 minutes")
        return BotFactory(
            configDirectory = File("./.config/"),
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
            .install(RuntimeCommand::installer)
            .install(Poll::installer)
            .install(WebSocketRemoteActions::installer)
            .create()

        /*return twitckBot(oauthToken, hostName) {
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
        }*/
    }
}

private fun ExtensionProvider.showImage(imagePath: String, text: String) {
    val path = javaClass.classLoader.getResourceAsStream(imagePath)
        ?: throw IllegalArgumentException("Failed to load image for resources: $imagePath")

    this.first(OverlayExtension::class)
        .showImage(path, text, Duration.ofSeconds(5))
}
