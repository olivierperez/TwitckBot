# Extension - Help

`!help` command to help the viewers to know what they can do.

```json5
{
  "extension": "Help",
  "enabled": true,
  "data": {
    "channel": "#<YOUR_CHANNEL_NAME>", // 1
    "commands": {
      "!name": "You'll never learn my real name!!" // 2
    }
  }
}
```

## Explanation

`// 1` This extension can react to a specific stream (for example: **"#gnu_coding_cafe"**).

`// 2` Configure here some simple commands that just make the bot saying something in the chat.

## Provides

The commands:
- `!help`
