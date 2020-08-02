# TwitckBot

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-orange.svg?style=flat-square)](http://makeapullrequest.com)
[![GitHub release](https://img.shields.io/github/release/olivierperez/TwitckBot.svg?style=flat-square)](https://GitHub.com/Naereen/StrapDown.js/releases/)

Every streamers need a bot, and it's not that easy to find the perfect bot that matches what you really want.

TwitckBot provides a good foundation to build your own bot, and allows you to extend it with whatever you're ready to develop.

# Installation

First of all, add the TwitckBot lib as dependency:

```groovy
// For Gradle
implementation "TODO-lib"
```

To work, TwitckBot only need a Twitch OAuth token.

```kotlin
fun main() {
    val yourChannel = "#gnucc"
    val oauthToken = "YOUR-OAUTH-TOKEN"

    val bot = twitckBot(oauthToken) {
    }

    bot.connectToServer()
    bot.send(yourChannel, "In position !")
    println("Ready to go!")
}
```

# Make him do things

The `twitckBot` function is the place you want to setup your bot, do to that you will have to **install** some **Extension**s.
The simplest one is `Welcome`, it will welcome everyone who is watching your stream:

```kotlin
fun main() {
    val yourChannel = "#gnucc"
    val hostName = "Olivier"
    val oauthToken = "YOUR-OAUTH-TOKEN"

    val bot = twitckBot(oauthToken) {
        install(Welcome) {
            channel(yourChannel)
            host(hostName, "Hey $hostName ! At your service.")
            addMessage("Hello #USER#, welcome!")
        }
    }

    bot.connectToServer()
    bot.send(yourChannel, "In position !")
    println("Ready to go!")
}
```

## Extensions

There's some extension you may want to use:

- `Channel`: The more opened extension, it allows you to do whatever you want
- `Help`: Responds to !help from the users
- `Presence`: Your bot will also connect (but won't operate) on a channel of your friend
- `Welcome`: Welcomes everyone
- `Whisper`: React to whispering

```groovy
// For Gradle
implementation "TODO-extensions"
```

## Make your own extension

TODO

# Example

TODO link to example

# 