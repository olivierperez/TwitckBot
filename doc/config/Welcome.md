# Extension - Welcome

Welcome the newcomers with funny sentences with their names in it.

```json5
{
    "extension": "Welcome",
    "enabled": true,
    "data": {
        "channel": "#<YOUR_CHANNEL_NAME>", // 1
        "streamId": "<Your streamer id>", // 2
        "secondsBetweenWelcomes": 7200, // 3
        "ignoreViewers": [
            "lurxx",
            "anotherttvviewer",
            "letsdothis_streamers"
        ],
        "messages": { // 4
            "forBroadcaster": [
                "Hey #USER#! Welcome home!"
            ],
            "forViewers": [
                "#USER# just arrived to the place to be",
                "#USER# almost downloaded all Wikipedia content"
            ],
            "forFollowers": [
                "Please give seat to #USER#",
                "Bring some water to #USER#, a beer is fine too"
            ]
        },
        "reactTo": { // 5
            "joins": false,
            "messages": true,
            "commands": false,
            "raids": true
        },
        "onWelcome": [
            // 6
        ]
    }
}

```

## Explanation

`// 1` A message to show at the bottom of the overlay.

`// 2` The twitch user id of your streamer account.

`// 3` Interval between two welcoming messages for the same viewer.

`// 4` Available messages to welcome viewers. Some messages are available for a group of viewers,
for instance you can specify the messages for followers.

`// 5` Define on which events you want to welcome your viewers.

`// 6` Execute some things when someone is welcomed by TwitckBot (see [Action steps](/doc/ActionSteps.md)).
