# Extension - RuntimeCommand

Let you configure some commands while you are in the middle of a streaming.

```json5
{
    "extension": "RuntimeCommand",
    "enabled": true,
    "data": {
        "channel": "#<YOUR_CHANNEL_NAME>", // 1
        "privilegedBadges": [ // 2
            "BROADCASTER",
            "MODERATOR"
        ]
    }
}
```

## Explanation

`// 1` A message to show at the bottom of the overlay.

`// 2` This block defines some privileged badges that will have more authorizations.
These badges can be `ADMIN`, `BROADCASTER`, `BITS`, `BITS_LEADER`, `FOUNDER`, `GLOBAL_MOD`,
`MODERATOR`, `SUBSCRIBER`, `STAFF`, `TURBO` and `VIP`.

## Provides

The commands:
- `!cmd` (privileged) (e.g. `!cmd permanent !gnu Let's go Gnu Coding Cafe`)