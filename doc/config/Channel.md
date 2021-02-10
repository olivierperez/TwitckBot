# Extension - Channel

Basic reactions to events or commands executed by the viewers.

```json5
{
  "extension": "Channel",
  "enabled": true,
  "data": {
    "channel": "#YOUR_CHANNEL_NAME", // 1
    "commands": {
      "!celebrate": [
        // 2
      ]
    },
    "follows": [
      // 3
    ]
  }
}
```

## Explanation

`// 1` This extension can react to a specific stream (for example: **"#gnu_coding_cafe"**).

`// 2` Execute some things when viewers enter the `!celebrate` command. Here you can choose the
command name you want, and you can set several commands.

`// 3` Execute some things when someone followed your channel (see [Action steps](/doc/ActionSteps.md)).
