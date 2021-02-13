# Extension - Poll

A poll mechanism to asked community their opinion.

```json5
{
    "extension": "Poll",
    "enabled": true,
    "data": {
        "channel": "#<YOUR_CHANNEL_NAME>", // 1
        "privilegedBadges": [ // 2
            "BROADCASTER",
            "MODERATOR"
        ],
        "pointsEarnPerVote": 25, // 3
        "i18n": { // 4
            "errorCreationPollUsage": "To create a poll: \"!poll <duration> <question>\"",
            "errorDurationIsMissing": "You have to choose a duration!",
            "newPoll": "New poll: #TITLE# Use !vote to vote",
            "pollHasJustFinished": "End of poll. #TITLE# #RESULTS#",
            "currentPollResult": "Poll is still opened... #TITLE# #RESULTS#",
            "oneResultFormat": "#ANSWER# (#COUNT#)",
            "pollHasNoVotes": "No-one answered to the question #TITLE#"
        }
    }
}
```

## Explanation

`// 1` This extension can react to a specific stream (for example: **"#gnu_coding_cafe"**).

`// 2` This block defines some privileged badges that will have more authorizations.
These badges can be `ADMIN`, `BROADCASTER`, `BITS`, `BITS_LEADER`, `FOUNDER`, `GLOBAL_MOD`,
`MODERATOR`, `SUBSCRIBER`, `STAFF`, `TURBO` and `VIP`.

`// 3` Reward the voters with some points.

`// 4` This block contains sentences that can be translated in the language of your stream.

## Provides

The commands:
- `!poll` (privileged)
- `!vote`
