# Action steps

Action steps are used to sequence executions when an event is triggered. For instance, it can be
used when a viewer becomes a follower, when a given command is entered, or when a viewer starts to
talk in the chat.

## How to configure

The short example, if you have a field "onWelcome" (as in Welcome's configuration file):
```json5
"onWelcome": [
    // here a list of executions
]
```

Longer example:
```json5
"onWelcome": [
    {
        "type": "sound",
        "sound": "celebration"
    },
    {
        "type": "message",
        "message": "Hého !"
    },
    {
        "type": "overlay_popup",
        "image": "image/vahine.png",
        "text": "Yata Yata Yata"
    }
]
```

## Execution types

### Command

Execute a command on behalf of the streamer.

```json5
{
    "type": "command",
    "command": "!cmd permanent !#USER# #PARAMS#" // Fully-qualified command to execute
}
```

### Sound

Play a sound that was configured in the Sound extension.

```json5
{
    "type": "sound",
    "sound": "celebration" // refers to the configuration of Sound extension
}
```

### Message

Send a message in the chat.

```json5
{
    "type": "message",
    "message": "Hého !"
}
```

### Overlay event

Show an event in the events box of the Overlay extension.

```json5
{
    "type": "overlay_event",
    "text": "#USER# arrive au Coding café" // a message to show in events box of Overlay extension
}
```

### Overlay popup

Show image (and option message) in the center of Overlay extension.

```json5
{
    "type": "overlay_popup",
    "image": "image/vahine.png", // file of the image to show
    "text": "Yata Yata Yata" // a optional message to show below the image
}
```

## Available variables

|Tag|Description|
|---|-----------|
|#USER#|Replaced by the viewer's display name.|
|#PARAMS#|Filled when triggered by a command, it contains the commands parameters.|
|#PARAM-(1, 2, etc.)#|Usable when triggered by a command, it is replaced of nth parameter of the command|
