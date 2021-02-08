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
      "style": { // 3
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

`// 3` The style of the boxes that wrap the texts.
