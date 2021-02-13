# Extension - ViewerPromotion

Some of your viewers deserve to be promoted.

```json5
{
    "extension": "Promotion",
    "enabled": true,
    "data": {
        "channel": "#<YOUR_CHANNEL_NAME>", // 1
        "secondsBetweenTwoPromotions": 7200, // 2
        "daysSinceLastVideoToPromote": 120, // 3
        "ignoreViewers": [ // 4
            "lurxx",
            "anotherttvviewer",
            "letsdothis_streamers"
        ],
        "promotionMessages": [ // 4
            "#USER# stream in category #GAME#, go see him right now #URL#"
        ],
        "i18n": { // 5
            "usage": "Usage: !shoutout <login>",
            "noPointsEnough": "#USER# you don't have enough points",
            "noAutoShoutOut": "Smart #USER#...",
            "shoutOutRecorded": "Message received!"
        }
    }
}
```

## Explanation

`// 1` This extension can react to a specific stream (for example: **"#gnu_coding_cafe"**).

`// 2` Interval between 2 promotions of the same viewer.

`// 3` Maximum interval between the last video of the viewer and now to consider the viewer is
still a streamer.

`// 4` List of available messages to promote the viewer.

`// 5` This block contains sentences that can be translated in the language of your stream.

## Provides

The commands:
- `!shoutout`
    - `!shoutout gnu_coding_cafe The one who developed the bot.` to register a shout out message
    - `!shoutout gnu_coding_cafe` to trigger the registered message
