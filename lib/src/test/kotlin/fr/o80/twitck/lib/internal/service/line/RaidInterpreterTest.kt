package fr.o80.twitck.lib.internal.service.line

import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.internal.handler.RaidDispatcher
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class RaidInterpreterTest {

    @MockK
    lateinit var messenger: Messenger

    @MockK
    lateinit var raidDispatcher: RaidDispatcher

    @MockK
    lateinit var logger: Logger

    private lateinit var raidInterpreter: RaidInterpreter

    @BeforeTest
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        raidInterpreter = RaidInterpreter(messenger, raidDispatcher, logger)
    }

    @Test
    fun shouldBeCalled() {
        raidInterpreter.handle("@badge-info=;badges=bits/100;color=#DAA520;display-name=skarab42;emotes=;flags=;id=azertyuiop;login=skarab42;mod=0;msg-id=raid;msg-param-displayName=skarab42;msg-param-login=skarab42;msg-param-profileImageURL=https://static-cdn.jtvnw.net/jtv_user_pictures/c8d08716-9712-460b-8fd2-2b0d0a46893d-profile_image-70x70.png;msg-param-viewerCount=18;room-id=124210976;subscriber=0;system-msg=18\\sraiders\\sfrom\\sskarab42\\shave\\sjoined!;tmi-sent-ts=1607713870096;user-id=485824438;user-type= :tmi.twitch.tv USERNOTICE #gnu_coding_cafe")

        verify {
            raidDispatcher.dispatch(any())
        }
    }

    @Test
    fun shouldNotBeCalled() {
        raidInterpreter.handle("@badge-info=;badges=bits/100;client-nonce=azertyuiop;color=#DAA520;display-name=skarab42;emote-only=1;emotes=304859604:0-8/304859612:10-18/304859670:20-31;flags=;id=azertyuiop;mod=0;room-id=124210976;subscriber=0;tmi-sent-ts=123456;turbo=0;user-id=485824438;user-type= :skarab42!skarab42@skarab42.tmi.twitch.tv PRIVMSG #gnu_coding_cafe :skarab1In skarab1Js skarab1Trust")

        verify(atLeast = 0, atMost = 0) {
            raidDispatcher.dispatch(any())
        }
    }

}
