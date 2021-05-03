# Extension - Overlay

An overlay to show on top of your stream.

```json5
{
  "extension": "Overlay",
  "enabled": true,
  "data": {
      "informativeText": {
          "text": "Yo les internets c'est Olivier !", // 1
          "anchor": "BottomRight" // 2
      },
      "events": { // 3
          "x": 1400,
          "y": 370,
          "width": 520,
          "height": 400,
          "fontSize": 20,
          "blockMargin": 10,
          "secondsToLeave": 300, // 4
          "showFrame": false
      },
      "style": { // 5
          "borderColor": "#080111",
          "backgroundColor": "#220632",
          "textColor": "#FFF9D9"
      }
  }
}
```

## Explanation

`// 1` A message to show at the bottom of the overlay.

`// 2` The position to draw this text (could be `BottomLeft`, `BottomCenter` or `BottomRight`).

`// 3` Setup the events box and how/where to draw it.
You can remove this part if you don't want to events to show.

`// 4` Number of seconds before an event stops showing.

`// 5` The style of the boxes that wrap the texts.
