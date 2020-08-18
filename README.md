# TwitckBot

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)
[![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-orange.svg?style=flat-square)](http://makeapullrequest.com)
[![Last release](https://jitpack.io/v/olivierperez/TwitckBot.svg?style=flat-square)](https://jitpack.io/#olivierperez/TwitckBot)

Every streamers need a bot, and it's not that easy to find the perfect bot matching what you really want.

TwitckBot provides a good foundation to build your own Twitch bot, and allows you to extend it with whatever you're ready to develop.

# Installation 👣

First of all, add the TwitckBot lib as dependency:

```groovy
// For Gradle
repositories {
    maven { url "https://jitpack.io" }
}

dependecies {
    implementation 'com.github.olivierperez.TwitckBot:lib:0.0.2'
}
```

To work, TwitckBot only need a Twitch OAuth token.

```kotlin
fun main() {
    val yourChannel = "#gnucc"
    val oauthToken = "YOUR-OAUTH-TOKEN"

    val bot = twitckBot(oauthToken) {
    }

    bot.connectToServer()
    bot.send(yourChannel, "In position!")
    println("Ready to go!")
}
```

# Make him do things 🧰

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
            host(hostName, "Hey $hostName! At your service.")
            addMessage("Hello #USER#, welcome!")
        }
    }

    bot.connectToServer()
    bot.send(yourChannel, "In position!")
    println("Ready to go!")
}
```

## Extensions

There's some extension you may want to use:

- `Channel`: The more opened extension, it allows you to do whatever you want
- `Help`: Responds to `!help` from the users
- `Presence`: Your bot will also connect (but won't operate) on a channel of your friend
- `RuntimeCommand`: You can add command at runtime thanks (eg: `!addcmd language Kotlin <3`)
- `Welcome`: Welcomes everyone
- `Whisper`: React to whispering

```groovy
// For Gradle
dependecies {
    implementation 'com.github.olivierperez.TwitckBot:extensions:0.0.2'
}
```

## Make your own extension 🎨

TODO

# Example

There's an [example](example/) to see how it could work.

## License 📄

```
    Copyright (C) 2020 Olivier Perez.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
```