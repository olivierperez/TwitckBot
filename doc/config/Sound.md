# Extension - Sound

Configure the funny sounds you need.

```json5
{
    "extension": "Sound",
    "enabled": true,
    "data": {
        "celebration": { // 1
            "path": "audio/celebration.wav",
            "gain": 1
        },
        "negative": { // 2
            "path": "audio/fail.wav",
            "gain": 1
        },
        "positive": { // 3
            "path": "audio/up.wav",
            "gain": 1
        },
        "raid": { // 4
            "path": "audio/raid.wav",
            "gain": 1
        },
        "custom": { // 5
            "buy": {
                "path": "audio/coin.wav",
                "gain": 1
            }
        }
    }
}
```

## Explanation

`// 1` to `// 4` Required sounds, you can't change the name of them.

`// 5` Custom sounds, you can add as many as you want.
