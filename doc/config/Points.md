# Extension - Points

The points system managed by the bot.

```json5
{
    "extension": "Points",
    "enabled": true,
    "data": {
        "channel": "#YOUR_CHANNEL_NAME", // 1
        "privilegedBadges": [ // 2
            "BROADCASTER",
            "MODERATOR"
        ],
        "i18n": { // 3
            "destinationViewerDoesNotExist": "Recipient is no-one",
            "pointsTransferred": "Points sent from #FROM# to #TO#",
            "notEnoughPoints": "Not enough point #FROM#",
            "viewerHasNoPoints": "#USER# has no points",
            "viewerHasPoints": "#USER# has #POINTS# points"
        }
    }
}
```

## Explanation

`// 1` This extension can react to a specific stream (for example: **"#gnu_coding_cafe"**).

`// 2` This block defines some privileged badges that will have more authorizations.
These badges can be `ADMIN`, `BROADCASTER`, `BITS`, `BITS_LEADER`, `FOUNDER`, `GLOBAL_MOD`,
`MODERATOR`, `SUBSCRIBER`, `STAFF`, `TURBO` and `VIP`.

`// 3` This block contains sentences that can be translated in the language of your stream.

## Provides

The commands:
- `!points`
- `!points_add` (privileged)
- `!points_give`
