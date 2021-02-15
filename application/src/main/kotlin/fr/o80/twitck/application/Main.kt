package fr.o80.twitck.application

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import fr.o80.twitck.extension.actions.WebSocketRemoteActions
import fr.o80.twitck.extension.channel.Channel
import fr.o80.twitck.extension.help.DefaultHelpExtension
import fr.o80.twitck.extension.market.Market
import fr.o80.twitck.extension.ngrok.NgrokTunnelExtension
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
import fr.o80.twitck.overlay.LwjglOverlay
import fr.o80.twitck.poll.Poll
import java.io.File

fun main(args: Array<String>) = Main().main(args)

class Main : CliktCommand() {

    private val oauthToken: String by option(help = "Oauth token of the bot").prompt("Bot's oauth token (oauth-token)")
    private val hostName: String by option(help = "Name of the host channel").prompt("Host's name (host-name)")

    override fun run() {
        val bot = configureBot(
            oauthToken = oauthToken.removePrefix("oauth:"),
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
            .install(Channel::installer)
            .install(NgrokTunnelExtension::installer)
            .create()
    }
}
