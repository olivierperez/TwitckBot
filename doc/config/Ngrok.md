# Extension - Ngrok

Auto-start ngrok when TwitckBot starts.

```json5
{
    "extension": "Ngrok",
    "enabled": true,
    "data": {
        "path": "<PATH/TO/ngrok.exe>", // 1
        "token": "<YOUR_NGROK_TOKEN>", // 2
        "name": "TwitckBot",
        "port": "8080"
    }
}
```

## Explanation

`// 1` Download the executable file of ngrok, and set its location here.

`// 2` Create an account on ngrok website and put your token here.
