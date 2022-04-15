import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import models.Point
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun DrawPlot(
    plotStartX: Float,
    plotEndX: Float,
    minHeight: Float,
    maxHeight: Float,
    modifier: Modifier = Modifier,
    stepToShowXCoordinates: Float = 1.0f,
    stepToShowYCoordinates: Float = stepToShowXCoordinates,
    canvasContent: CanvasDrawScope.() -> Unit
) {
    //inversion is done because pixels on screen are 0 on top and some positive number on bottom
    val minY = min(-minHeight, -maxHeight)
    val maxY = max(-minHeight, -maxHeight)
    Canvas(
        modifier = modifier
    ) {
        val OYAxis = getAxis(size.width, plotStartX, plotEndX)
        val OXAxis = getAxis(size.height, minY, maxY)
        OYAxis?.let {
            drawLine(Color.Gray, Offset(it, 0f), Offset(it, size.height - 1))
            drawYCoordinates(minY, maxY, OYAxis, stepToShowYCoordinates)
        }
        OXAxis?.let {
            drawLine(Color.Gray, Offset(0f, it), Offset(size.width - 1, it))
            drawXCoordinates(plotStartX, plotEndX, it, stepToShowXCoordinates)
        }
        val canvasDrawScope = CanvasDrawScope(this,
            _mapCoordinateToOffset = { x, y ->
                Offset(
                    mapCoordinateToPixel(x, size.width, plotStartX, plotEndX),
                    mapCoordinateToPixel(-y, size.height, minY, maxY)
                )
            },
            _mapPointToPixel = { x, y ->
                Point(
                    mapCoordinateToPixel(x, size.width, plotStartX, plotEndX),
                    mapCoordinateToPixel(-y, size.height, minY, maxY)
                )
            },
            _mapPixelToPoint = { x, y ->
                Point(
                    mapPixelToCoordinate(x, size.width, plotStartX, plotEndX),
                    -mapPixelToCoordinate(y, size.height, minY, maxY)
                )

            })
        canvasDrawScope.canvasContent()
    }
}

private fun DrawScope.drawXCoordinates(a: Float, b: Float, OXAxis: Float, stepToShowXCoordinates: Float) {
    val aRounded = a.round(stepToShowXCoordinates)
    val bRounded = b.round(stepToShowXCoordinates)
    val listOfPoints = List((bRounded - aRounded).roundToInt()) {
        mapCoordinateToPixel(aRounded + it * stepToShowXCoordinates, size.width, a, b)
    }
    for (point in listOfPoints) {
        drawLine(Color.Gray, Offset(point, OXAxis - 5f), Offset(point, OXAxis + 5f))
    }
}

private fun DrawScope.drawYCoordinates(minY: Float, maxY: Float, OYAxis: Float, stepToShowYCoordinates: Float) {
    val aRounded = minY.round(stepToShowYCoordinates)
    val bRounded = maxY.round(stepToShowYCoordinates)
    val listOfPoints = List((bRounded - aRounded).roundToInt()) {
        mapCoordinateToPixel(aRounded + it * stepToShowYCoordinates, size.height, minY, maxY)
    }
    for (point in listOfPoints) {
        drawLine(Color.Gray, Offset(OYAxis - 5f, point), Offset(OYAxis + 5f, point))
    }
}

private fun Float.round(precision: Float): Float = this - this % precision

fun mapPixelToCoordinate(
    pixelToMap: Float,
    windowSize: Float,
    firstCoordinate: Float,
    secondCoordinate: Float
): Float {
    val start = min(firstCoordinate, secondCoordinate)
    val end = max(firstCoordinate, secondCoordinate)
    return pixelToMap * (end - start) / windowSize + start
}

fun mapCoordinateToPixel(
    coordinateToMap: Float,
    windowSize: Float,
    firstCoordinate: Float,
    secondCoordinate: Float
): Float {
    val start = min(firstCoordinate, secondCoordinate)
    val end = max(firstCoordinate, secondCoordinate)
    return ((coordinateToMap - start) * windowSize / (end - start))
}


private fun getAxis(windowSize: Float, firstCoordinate: Float, secondCoordinate: Float): Float? {
    return if (doesCoordinatesHaveDifferentSign(firstCoordinate, secondCoordinate)) {
        mapCoordinateToPixel(0.0f, windowSize, firstCoordinate, secondCoordinate)
    } else {
        null
    }
}

private fun doesCoordinatesHaveDifferentSign(firstCoordinate: Float, secondCoordinate: Float): Boolean {
    return firstCoordinate * secondCoordinate <= 0
}

class CanvasDrawScope(
    drawScope: DrawScope,
    private var _mapCoordinateToOffset: (x: Float, y: Float) -> Offset,
    private var _mapPointToPixel: (x: Float, y: Float) -> Point,
    private var _mapPixelToPoint: (x: Float, y: Float) -> Point,
) :
    DrawScope by drawScope {
    fun mapCoordinateToOffset(point: Point) = _mapCoordinateToOffset(point.x, point.y)
    fun mapPointToPixel(point: Point) = _mapPointToPixel(point.x, point.y)
    fun mapPixelToPoint(point: Point) = _mapPixelToPoint(point.x, point.y)
}