import algorithms.calculateVisibility
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
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
            MainContent()
        }
    }
}

@Composable
fun MainContent() {
    Row {
        if (isModifying.value) {
            Menu()
            Surface(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color.Black) {

            }
        }
        Plot()
    }
}

@Composable
fun Menu() {
    Column(modifier = Modifier.fillMaxWidth(0.25f)) {
        Button(
            onClick = {
                isModifying.value = false
            },
            enabled = isModifying.value,
            modifier = Modifier
        ) {
            Text("Припинити редагування")
        }
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(currentPoints.value, { _, item -> item }) { index, item ->
                Row {
                    Button(onClick = {
                        val newList = currentPoints.value.toMutableList()
                        newList.removeAt(index)
                        currentPoints.value = newList
                    }) {
                        Text("Видалити")
                    }
                    Column {
                        Row {
                            Text("X: ")
                            TextField(item.x.toString(), onValueChange = { string ->
                                val value = string.toFloatOrNull()
                                value?.let {
                                    val newList = currentPoints.value.toMutableList()
                                    newList[index] = Point(it, item.y)
                                    currentPoints.value = newList
                                }
                            })
                        }
                        Row {
                            Text("Y: ")
                            TextField(item.y.toString(), onValueChange = { string ->
                                val value = string.toFloatOrNull()
                                value?.let {
                                    val newList = currentPoints.value.toMutableList()
                                    newList[index] = Point(item.x, it)
                                    currentPoints.value = newList
                                }
                            })
                        }
                    }
                }
            }
        }
        Button(onClick = {
            currentPoints.value = mutableListOf()
        }) {
            Text("Очистити")
        }
    }
}

@Composable
fun Plot() {
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
                        handlePointerEvent(event)
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


fun handlePointerEvent(event: PointerEvent) {
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