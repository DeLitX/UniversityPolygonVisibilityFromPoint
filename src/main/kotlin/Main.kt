import algorithms.calculateVisibility
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import models.Edge
import models.Point
import models.Polygon
import models.toPoint

val polygon: MutableState<Polygon?> = mutableStateOf(null)
val visibleEdges: MutableState<List<Edge>> = mutableStateOf(listOf())
val invisibleEdges: MutableState<List<Edge>> = mutableStateOf(listOf())
val observationPoint: MutableState<Point?> = mutableStateOf(null)
val pixelObservationPoint: MutableState<Point?> = mutableStateOf(null)
val currentPoints: MutableState<List<Point>> = mutableStateOf(listOf())
val isModifying: MutableState<Boolean> = mutableStateOf(true)
val currentPoint: MutableState<Point?> = mutableStateOf(null)

fun main() {
    observationPoint.value = Point(1f, 1f)
    Window {
        MaterialTheme {
            Row {
                Button(
                    onClick = {
                        isModifying.value = false
                    },
                    enabled = isModifying.value,
                    modifier = Modifier
                ) {
                    Text("Припинити редагування")
                }
                DrawPlot(
                    plotStartX = -2f,
                    plotEndX = 2f,
                    minHeight = -2f,
                    maxHeight = 2f,
                    modifier = Modifier.fillMaxSize()
                        .pointerInput("aboba", "a") {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.isNotEmpty()) {
                                        val firstEvent = event.changes.first()
                                        val position = firstEvent.position
                                        if (position.x >= 0 && position.y >= 0) {
                                            if (isModifying.value) {
                                                if (firstEvent.pressed) {
                                                    currentPoint.value = position.toPoint()
                                                } else {
                                                    if (currentPoint.value != null) {
                                                        currentPoint.value = null
                                                        currentPoints.value = currentPoints.value + position.toPoint()
                                                        println(position)
                                                    }
                                                }
                                            } else {
                                                if (firstEvent.pressed) {
                                                    pixelObservationPoint.value = position.toPoint()
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }) {
                    if (isModifying.value) {
                        val polygon = Polygon(points = currentPoints.value.map { mapPixelToPoint(it) })
                        DrawPolygon(polygon)
                    } else {
                        if (polygon.value == null) {
                            polygon.value = Polygon(points = currentPoints.value.map { mapPixelToPoint(it) })
                            recalculateVisibleEdges()
                            println(polygon)
                        }
                        polygon.value?.let {
                            if (pixelObservationPoint.value == null && observationPoint.value != null) {
                                pixelObservationPoint.value = mapPointToPixel(observationPoint.value!!)
                            }
                            val pixelPointToOffset = mapPixelToPoint(pixelObservationPoint.value!!)
                            if (pixelPointToOffset != observationPoint.value) {
                                observationPoint.value = pixelPointToOffset
                                recalculateVisibleEdges()
                            }
                            DrawEdges(visibleEdges.value, SolidColor(Color(255, 0, 0, 255)))
                            DrawEdges(invisibleEdges.value, SolidColor(Color(0, 0, 255, 255)))
                            DrawLinesToVisibleEdges(visibleEdges.value, observationPoint.value!!)
                        }
                    }
                }
            }
        }
    }
}

fun recalculateVisibleEdges() {
    val visibilityLines = calculateVisibility(polygon.value!!, observationPoint.value!!)
    visibleEdges.value = visibilityLines.visibleEdges
    invisibleEdges.value = visibilityLines.invisibleEdges
}

fun CanvasDrawScope.DrawPolygon(polygon: Polygon) {
    for (edge in polygon.edges) {
        drawLine(
            SolidColor(Color(0, 0, 0, 255)),
            start = mapCoordinateToOffset(edge.point1),
            end = mapCoordinateToOffset(edge.point2)
        )
    }
}

fun CanvasDrawScope.DrawEdges(edges: List<Edge>, brush: Brush) {
    for (edge in edges) {
        drawLine(
            brush = brush,
            start = mapCoordinateToOffset(edge.point1),
            end = mapCoordinateToOffset(edge.point2)
        )
    }
}

fun CanvasDrawScope.DrawLinesToVisibleEdges(edges: List<Edge>, observablePoint: Point) {
    for (edge in edges) {
        drawLine(
            SolidColor(Color(255, 0, 0, 255)),
            start = mapCoordinateToOffset(observablePoint),
            end = mapCoordinateToOffset(edge.point1)
        )
        drawLine(
            SolidColor(Color(255, 0, 0, 255)),
            start = mapCoordinateToOffset(observablePoint),
            end = mapCoordinateToOffset(edge.point2)
        )
    }
}