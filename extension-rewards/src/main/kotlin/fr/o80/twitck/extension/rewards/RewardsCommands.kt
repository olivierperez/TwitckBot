package fr.o80.twitck.extension.rewards

import fr.o80.twitck.lib.api.bean.Viewer
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.extension.PointsExtension
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.service.time.TimeChecker
import java.time.Duration

class RewardsCommands(
    private val channel: String,
    private val claimConfig: RewardsClaim,
    private val i18n: RewardsI18n,
    private val claimTimeChecker: TimeChecker,
    private val points: PointsExtension,
    private val overlay: OverlayExtension?,
    private val sound: SoundExtension?
) {

    fun interceptCommandEvent(commandEvent: CommandEvent): CommandEvent {
        if (channel != commandEvent.channel)
            return commandEvent

        when (commandEvent.command.tag) {
            claimConfig.command -> claim(commandEvent.viewer)
        }

        return commandEvent
    }

    private fun claim(viewer: Viewer) {
        if (claimConfig.reward == 0) return

        claimTimeChecker.executeIfNotCooldown(viewer.login) {
            points.addPoints(viewer.login, claimConfig.reward)

            val message = i18n.viewerJustClaimed
                .replace("#USER#", viewer.displayName)
                .replace("#NEW_POINTS#", claimConfig.reward.toString())
                .replace("#OWNED_POINTS#", points.getPoints(viewer.login).toString())

            playCoin()
            displayCoinAndMessage(message)
        }.fallback {
            playFail()
        }
    }

    private fun playCoin() {
        sound?.play(claimConfig.positiveSound)
    }

    private fun playFail() {
        sound?.play(claimConfig.negativeSound)
    }

    private fun displayCoinAndMessage(message: String) {
        overlay?.showImage(claimConfig.image, message, Duration.ofSeconds(5))
    }

}
