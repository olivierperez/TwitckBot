package fr.o80.twitck.overlay

import fr.o80.twitck.lib.api.bean.Color
import fr.o80.twitck.overlay.graphics.ext.Vertex3f

fun Color.toVertex3f(): Vertex3f {
    return Vertex3f(
        red / 255f,
        green / 255f,
        blue / 255f
    )
}
