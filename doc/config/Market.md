# Extension - Market

A marketplace where the viewers can "buy" things with your points system.

```json5
{
    "extension": "Market",
    "enabled": true,
    "data": {
        "channel": "#<YOUR_CHANNEL_NAME>", // 1
        "i18n": { // 2
            "productNotFound": "The product doesn't exist!",
            "usage": "Usage of !buy => !buy <produit> <paramètres>",
            "weHaveThisProducts": "Here is what I can sell: #PRODUCTS#",
            "youDontHaveEnoughPoints": "@#USER# you're too poor to afford this product!"
        },
        "products": [
            { // 3
                "name": "command",
                "price": 200,
                "steps": [ // 4
                    {
                        "type": "command",
                        "command": "!cmd permanent !#USER# #PARAMS#"
                    },
                    {
                        "type": "sound",
                        "sound": "positive"
                    }
                ]
            }
        ],
        "rewards": [
            { // 5
                "id": "1234abcd-ab12-cd34-ef56-0123456789abcdef", // 6
                "steps": [ // 7
                    {
                        "type": "command",
                        "command": "!cmd permanent !#USER# #PARAMS#"
                    },
                    {
                        "type": "sound",
                        "sound": "positive"
                    }
                ]
            }
        ]
    }
}
```

## Explanation

`// 1` This extension can react to a specific stream (for example: **"#gnu_coding_cafe"**).

`// 2` This block contains sentences that can be translated in the language of your stream.

`// 3` This block defines one product available in your marketplace, you can configure several
products below. Each product have to define : a name, a price and a list of things to do.

`// 4` The steps are the things to do when someone bought the product (see [Action steps](/doc/ActionSteps.md)).. In the example above, two
things will happen:
1. Execution of the command on behalf of the streamer
2. A sound (configure in Sound extension) will be played

`// 5` This block defines the Twitch rewards available with channel points of Twitch.

`// 6` The id of the Twitch reward (you can find it in the log of TwitckBot).

`// 7` Same as `// 4`

## Provides

The commands:
- `!market`
- `!buy` (e.g. `!buy command Of everything, look to the sunny part.` then `!<YOUR_NAME>`)
