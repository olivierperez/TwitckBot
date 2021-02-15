# Extension - Repeat

The bot will say one of predefined sentences every X minutes. Useful to give information about a Discord server for instance.

```json5
{
    "extension": "Repeat",
    "enabled": true,
    "data": {
        "channel": "#<YOUR_CHANNEL_NAME>", // 1
        "secondsBetweenRepeatedMessages": 900, // 2
        "messages": [ // 3
            "Welcome to the place to be",
            "Come say hi on discord: https://.../"
        ]
    }
}
```

## Explanation

`// 1` A message to show at the bottom of the overlay.

`// 2` The interval (in seconds) between 2 messages from this extension.

`// 3` A list of available messages in which the extension will randomly pick.
