# TwitckBot

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)
[![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-orange.svg?style=flat-square)](http://makeapullrequest.com)
[![Last release](https://jitpack.io/v/olivierperez/TwitckBot.svg?style=flat-square)](https://jitpack.io/#olivierperez/TwitckBot)

Every streamer need a sidekick, someone that is by his side when he streams. TwitckBot is this sidekick, always by your side day and night.

On top of that, if you are a developer you can use TwitckBot as a foundation to develop your own Twitch Bot.

# 👣 Install me

## 🧲 Download

**Download the zip file** of the last release from the
[Releases page](https://github.com/olivierperez/TwitckBot/releases).

**Unzip it** and go to the next chapter.

## ⚙ Configure the ngrok file

Ngrok is used to react to events of viewer following.
So first: [download the executable file of ngrok](https://ngrok.com/download).

Once downloaded, edit the ngrok file regarding your operating system:
- Windows: **ngrok.bat**
- Linux: **ngrok.sh**

In this file, replace `<PATH\TO\ngrok.exe>` by the path of the previously downloaded `.exe` file.

## ⚙ Configure the launch file

Edit the TwitckBot launch file regarding your operating system:
- Windows: **TwitckBot.bat**
- Linux: **TwitckBot.sh**

In this file, replace `<OAUTH_TOKEN>` by the token of your bot account (it can be your own account if want the bot talk on your behalf),
and replace `<STREAMER_ACCOUNT_NAME>` by your own twitch account's name (even if the bot has its own account).

tips: to generate a OAuth token you may use this generator: https://twitchapps.com/tmi/

## ⚙ Configure the modules

TwitckBot is composed of several modules (known as Extensions), those modules are configurable
thanks to `.json` files in the `.config` directory.

|Extension|Description|Requires|Can interact with|Configuration|
|---------|-----------|--------|-----------------|-------------|
|Channel|Basic reactions to events or commands executed by the viewers.|-|-|[documentation](doc/config/Channel.md)|
|Help|`!help` command to help the viewers to know what they can do.|-|-|[documentation](doc/config/Help.md)|
|Market|A marketplace where the viewers can "buy" things with your points system.|-|Help</br>Points|[documentation](doc/config/Market.md)|
|Ngrok|**Required** to react to events of viewer following.|-|-|[documentation](doc/config/Ngrok.md)|
|Overlay|An overlay to show on top of your stream.|-|-|[documentation](doc/config/Overlay.md)|
|Points|The points system managed by the bot.|Storage|Help|[documentation](doc/config/Points.md)|
|Poll|A poll mechanism to asked community their opinion.|-|Points|[documentation](doc/config/Poll.md)|
|RemoteActions|*Under development!!*<br/>*Requires Streamlab OBS!!*<br/>Provide a UI for the streamer to interact with his bot.|Storage|-|[documentation](doc/config/RemoteActions.md)|
|Repeat|The bot will say one of predefined sentences every X minutes. Useful to give information about a Discord server for instance.|-|-|[documentation](doc/config/Repeat.md)|
|Rewards|Reward with points the activity of the viewers.|Points<br/>Storage|Help<br/>Overlay<br/>Sound|[documentation](doc/config/Rewards.md)|
|RuntimeCommand|Let you configure some commands while you are in the middle of a streaming.|-|Help<br/>Storage|[documentation](doc/config/RuntimeCommand.md)|
|Sound|Configure the funny sounds you need.|-|-|[documentation](doc/config/Sound.md)|
|Stats|*Under development!!* Store stats of what the bot have seen during your stream.|-|-|[documentation](doc/config/Stats.md)|
|Storage|Store information useful for your stream.|-|-|[documentation](doc/config/Storage.md)|
|ViewerPromotion|Some of your viewers deserve to be promoted.|Storage|Help<br/>Points<br/>Sound|[documentation](doc/config/ViewerPromotion.md)|
|Welcome|Welcome the newcomers with funny sentences with their names in it.|Storage|Sound|[documentation](doc/config/Welcome.md)|

# ▶ Run

1. Execute "ngrok.bat" (or "ngrok.sh" on Linux)
   - please, avoid restarting it while you're still streaming
2. Execute "TwitckBot.bat" (or "TwitckBot.sh" on Linux)
3. Wait to see a big "Ready to go!"...
4. TwitckBot is up and running, type a command (`!help` for instance) in Twitch chat

## 📄 License

```
Copyright (C) 2021 Olivier Perez.

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