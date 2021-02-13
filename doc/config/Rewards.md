# Extension - Rewards

Reward with points the activity of the viewers.

```json5
{
    "extension": "Rewards",
    "enabled": true,
    "data": {
        "channel": "#<YOUR_CHANNEL_NAME>", // 1
        "talk": { // 2
            "reward": 5,
            "secondsBetweenTwoTalkRewards": 300
        },
        "claim": { // 3
            "command": "!claim",
            "reward": 15,
            "secondsBetweenTwoClaims": 1200,
            "image": "image/coin.png",
            "positiveSound": "buy",
            "negativeSound": "negative"
        },
        "i18n": { // 4
            "viewerJustClaimed": "#USER# vient de collecter #NEW_POINTS# codes source et en possède donc #OWNED_POINTS#"
        }
    }
}
```

## Explanation

`// 1` A message to show at the bottom of the overlay.

`// 2` This block defines the rewards for talkative viewers.

`// 3` This block defines the rewards for a viewer that uses the `!claim` command.

`// 4` This block contains sentences that can be translated in the language of your stream.

## Provides

The commands:
- `!claim`
