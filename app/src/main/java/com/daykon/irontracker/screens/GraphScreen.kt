package com.daykon.irontracker.screens

import android.content.res.Configuration
import android.graphics.PointF
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.daykon.irontracker.db.Database
import com.daykon.irontracker.db.ExerciseRecord
import com.daykon.irontracker.viewModels.GraphViewModel
import com.daykon.irontracker.viewModels.state.GraphState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.daykon.irontracker.viewModels.events.GraphEvent
import kotlin.math.sqrt

@OptIn(ExperimentalTextApi::class)
@Composable
fun Graph(
    records: List<ExerciseRecord>,
    mode: String = "reps", // Possible values ["reps", "weights"],
    color: Color = Color(0x00000000),
    state: GraphState,
    onEvent: (GraphEvent) -> Unit,
    onGraphClick: (DpOffset) -> Unit
) {
    var minValue = 9999999f
    var maxValue = 0f
    var minDate: LocalDateTime = LocalDateTime.MAX
    var maxDate: LocalDateTime = LocalDateTime.MIN
    var isThisVisible by remember {
        mutableStateOf(false)
    }
    records.forEach { record ->
        if (record.date < minDate) {
            minDate = record.date
        }
        if (record.date > maxDate) {
            maxDate = record.date
        }
        val value = if (mode == "reps") {
            record.reps.toFloat()
        } else {
            record.weight
        }
        if (value < minValue) {
            minValue = value
        }
        if (value > maxValue) {
            maxValue = value
        }
    }
    val minutesBetween = ChronoUnit.MINUTES.between(minDate, maxDate)
    val dateTimeFormatterMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")
    val dateTimeFormatterYear: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var pressOffset by remember {
        mutableStateOf(DpOffset.Zero)
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .onSizeChanged {
                boxSize = it
            },
        contentAlignment = Alignment.Center
    ) {

        val textMeasurer = rememberTextMeasurer()
        val configuration = LocalConfiguration.current


        val screenHeight: Float = boxSize.height.toFloat()
        val screenWidth: Float = boxSize.width.toFloat()

        val paddingX0: Dp = 35.dp
        val paddingX1: Dp = 30.dp
        val paddingY0: Dp = 50.dp
        val paddingY1: Dp = 10.dp
        val paddingX0Px = with(LocalDensity.current) { paddingX0.toPx() }
        val paddingX1Px = with(LocalDensity.current) { paddingX1.toPx() }
        val paddingY0Px = with(LocalDensity.current) { paddingY0.toPx() }
        val paddingY1Px = with(LocalDensity.current) { paddingY1.toPx() }

        val xAxisSpace = (screenWidth - paddingX0Px - paddingX1Px)
        val yAxisSpace = (screenHeight - paddingY0Px - paddingY1Px)

        var minValueInt: Int = minValue.roundToInt()
        var maxValueInt: Int = maxValue.roundToInt()
        if (minValueInt == maxValueInt) {
            minValueInt -= 1
            maxValueInt += 1
        }
        var reduceMin = true
        val divisionValue =
            if (configuration.layoutDirection == Configuration.ORIENTATION_LANDSCAPE) {
                200
            } else {
                100
            }
        val numMarksY: Int = (yAxisSpace / divisionValue).roundToInt()
        val numMarksX: Int = (xAxisSpace / 300).roundToInt()
        while ((maxValueInt - minValueInt) % numMarksY != 0) {
            if (reduceMin && minValueInt > 0) {
                minValueInt -= 1
            } else if (!reduceMin) {
                maxValueInt += 1
            }
            reduceMin = !reduceMin
        }
        val intervalY: Int = (maxValueInt - minValueInt) / numMarksY
        val intervalX: Long = minutesBetween / numMarksX + 1

        val coordinates: ArrayList<PointF> = ArrayList()

        fun formatTime(i: Int): String {
            val time: LocalDateTime = maxDate.minusMinutes((numMarksX - i) * intervalX)
            if (minutesBetween > 365 * 24 * 60)
                return time.format(dateTimeFormatterYear)
            return time.format(dateTimeFormatterMonth)
        }

        fun getXYValuesRecord(record: ExerciseRecord): Array<Float> {
            val date: Long = ChronoUnit.MINUTES.between(minDate, record.date)
            val value = if (mode == "reps") {
                record.reps.toFloat()
            } else {
                record.weight
            }
            val x1: Float = xAxisSpace * date / minutesBetween + paddingX0Px
            Log.d(
                "DEBUG",
                minValueInt.toString() + " " + maxValueInt.toString() + " " + value.toString() + " | " + ((value - minValueInt) / (maxValueInt - minValueInt)).toString()
            )
            var y1: Float = yAxisSpace * (1 - (value - minValueInt) / (maxValueInt - minValueInt))
            if (configuration.layoutDirection == Configuration.ORIENTATION_LANDSCAPE) {
                y1 = yAxisSpace - y1
            }

            return arrayOf(x1, y1)
        }

        fun getInitialPositions(records: List<ExerciseRecord>): ArrayList<Array<Float>> {
            val positions = ArrayList<Array<Float>>()
            for (i in records.indices) {
                positions.add(getXYValuesRecord(records[i]))
            }
            return positions
        }

        val positions = getInitialPositions(records)

        if (!state.isBoxVisible) {
            isThisVisible = false
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(screenHeight, state) {
                    detectTapGestures(
                        onTap = {
                            if (state.isBoxVisible) {
                                onEvent(GraphEvent.SetBoxVisibility(false))
                                //onEvent(GraphEvent.SetSelectedPoint(-1))
                            } else {
                                isThisVisible = true
                                pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                                var closest: Int = -1
                                var distance: Float = Float.MAX_VALUE
                                for (i in positions.indices) {
                                    val d: Float = sqrt(
                                        ((positions[i][0] - pressOffset.x.toPx()) * (positions[i][0] - pressOffset.x.toPx())) +
                                                ((positions[i][1] - pressOffset.y.toPx()) * (positions[i][1] - pressOffset.y.toPx()))
                                    )
                                    if (d < distance) {
                                        distance = d
                                        closest = i
                                    }
                                }
                                Log.d("Closest", closest.toString())
                                if (distance < 150) {
                                    onEvent(GraphEvent.SetSelectedPoint(closest))
                                    onEvent(GraphEvent.SetBoxVisibility(true))
                                    onGraphClick(DpOffset(positions[closest][0].toDp(), positions[closest][1].toDp()))
                                } else {
                                    onEvent(GraphEvent.SetSelectedPoint(-1))
                                    //onEvent(GraphEvent.SetBoxVisibility(false))
                                }

                            }
                        }
                    )
                })
        {
            var j = 0
            while (j < numMarksX + 1) {
                val measuredText: TextLayoutResult =
                    textMeasurer.measure(AnnotatedString(formatTime(j)))
                drawText(
                    textMeasurer, formatTime(j),
                    Offset(
                        xAxisSpace / numMarksX * (j) - measuredText.getLineRight(0) / 2 + paddingX0.toPx(),
                        screenHeight - 50
                    ),
                    style = TextStyle.Default.copy(color = onBackgroundColor)
                )
                j += 1
            }
            j = 0
            /** placing y axis points */
            while (j < numMarksY + 1) {
                val text: String = (minValueInt + j * intervalY).toString()
                val measuredText: TextLayoutResult = textMeasurer.measure(AnnotatedString(text))
                drawText(
                    textMeasurer,
                    text,
                    Offset(
                        0f,
                        screenHeight - yAxisSpace / numMarksY * (j) -
                                measuredText.getLineBottom(0) - paddingY0.toPx()
                    ),
                    style = TextStyle.Default.copy(color = onBackgroundColor)
                )
                j += 1
            }

            val title = if (mode == "reps") {
                "Reps"
            } else {
                "Weight (kg)"
            }

            val measuredText: TextLayoutResult = textMeasurer.measure(AnnotatedString(title))
            drawText(
                textMeasurer,
                title,
                Offset(
                    xAxisSpace - measuredText.getLineRight(0) + paddingX0.toPx(),
                    yAxisSpace - paddingY1.toPx()
                ),
                style = TextStyle.Default.copy(color = onBackgroundColor)
            )


            /** Draw coordinate axis */
            val axis = Path().apply {
                reset()
                moveTo(paddingX0.toPx(), size.height - paddingY0.toPx())
                lineTo(paddingX0.toPx(), 0f)
                moveTo(paddingX0.toPx(), size.height - paddingY0.toPx())
                lineTo(paddingX0.toPx() + xAxisSpace, size.height - paddingY0.toPx())

            }


            /** placing points */
            for (i in records.indices) {
                val pos: Array<Float> = positions[i]
                val x1: Float = pos[0]
                val y1: Float = pos[1]




                coordinates.add(PointF(x1, y1))

                Log.d("ClosestIn", "${coordinates[i].x} ${coordinates[i].y}")
                /** drawing circles to indicate all the points */
                drawCircle(
                    color = color,
                    radius = if (state.selectedPoint == i) 15f else 5f,
                    center = Offset(x1, y1)
                )
                if (mode != "reps") {
                    Log.d("coordinates", i.toString() + " " + PointF(x1, y1).toString())
                }
            }


            val controlPoints1: ArrayList<PointF> = ArrayList()
            val controlPoints2: ArrayList<PointF> = ArrayList()

            for (i in 1 until coordinates.size) {
                controlPoints1.add(
                    PointF(
                        (coordinates[i].x + coordinates[i - 1].x) / 2,
                        coordinates[i - 1].y
                    )
                )
                controlPoints2.add(
                    PointF(
                        (coordinates[i].x + coordinates[i - 1].x) / 2,
                        coordinates[i].y
                    )
                )
            }

            if (coordinates.size > 1) {
                val stroke = Path().apply {
                    reset()
                    moveTo(coordinates.first().x, coordinates.first().y)
                    for (i in 0 until coordinates.size) {
                        lineTo(
                            coordinates[i].x, coordinates[i].y
                        )
                    }
                }

                drawPath(
                    stroke,
                    color = color,
                    style = Stroke(
                        width = 5f,
                        cap = StrokeCap.Square

                    )
                )
                val fillPath = android.graphics.Path(stroke.asAndroidPath())
                    .asComposePath()
                    .apply {
                        lineTo(
                            getXYValuesRecord(records[records.size - 1])[0],
                            size.height - paddingY0.toPx()
                        )
                        lineTo(paddingX0.toPx(), size.height - paddingY0.toPx())
                        close()
                    }
                drawPath(
                    fillPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            color,
                            Color.Transparent,
                        ),
                        endY = size.height - paddingY0.toPx() - 30
                    ),
                )
                drawPath(
                    axis,
                    color = onBackgroundColor,
                    style = Stroke(
                        width = 5f,
                        cap = StrokeCap.Square

                    )
                )

            }
        }

    }
    Log.d("TEST", state.isBoxVisible.toString())

}

@ExperimentalMaterial3Api
@Composable
fun GraphScreen(
    db: Database,
    exerciseId: String = "0"
) {
    val graphViewModel = GraphViewModel(db.exerciseDao, db.exerciseRecordDao, exerciseId.toInt())
    val state = graphViewModel.state.collectAsState()
    val onEvent: (GraphEvent) -> Unit = graphViewModel::onEvent
    val density = LocalDensity.current
    val dateTimeFormatterYear: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")

    var pressOffset by remember {
        mutableStateOf(DpOffset.Zero)
    }

    var itemHeight by remember {
        mutableStateOf(0.dp)
    }
    var cardHeight by remember {
        mutableStateOf(0.dp)
    }
    var cardSubHeight by remember {
        mutableStateOf(0.dp)
    }
    var boxSizeX by remember {
        mutableStateOf(0.dp)
    }
    var boxSizeY by remember {
        mutableStateOf(0.dp)
    }
    val context = LocalContext.current
    val scaffoldPadding: Dp = 8.dp
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val stableItemHeight = rememberUpdatedState(itemHeight)
    val stableCardHeight = rememberUpdatedState(cardHeight)
    val stableCardSubHeight = rememberUpdatedState(cardHeight)


    Log.d("AYYITEMHEIGHT", itemHeight.toString())

    Box(modifier = Modifier.pointerInput(null) {
        detectTapGestures(
            onTap = {
                Log.d("ONTAP", DpOffset(it.x.toDp(), it.y.toDp()).toString())

                if (state.value.isBoxVisible) {
                    onEvent(GraphEvent.SetBoxVisibility(false))
                } else {
                    pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                }
            })
    }) {
        Scaffold { padding ->
            Column(modifier = Modifier.padding(PaddingValues(scaffoldPadding, scaffoldPadding, scaffoldPadding, 0.dp))) {

                Row(modifier = Modifier.onSizeChanged {
                    cardHeight = with(density) { it.height.toDp() }
                })
                {
                    ElevatedCard(
                        modifier = Modifier.wrapContentSize(),

                        shape = CutCornerShape(15, 0, 15, 0),
                        colors = CardDefaults.cardColors(containerColor = Color(state.value.exercise.muscleGroup.color))
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .wrapContentSize()
                                .fillMaxWidth()
                        ) {
                            Text(
                                state.value.exercise.exercise.name,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
                                color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background
                                else MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PaddingValues(0.dp, 8.dp, 0.dp, 0.dp))
                        .onSizeChanged {
                            cardSubHeight = with(density) { it.height.toDp() }
                        }

                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(state.value.exercise.muscleGroup.name)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .onSizeChanged {
                            itemHeight = with(density) { it.height.toDp() }
                        }
                ) {
                    if (state.value.exerciseRecords.size < 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.CenterVertically),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Not enough records yet", textAlign = TextAlign.Center)
                        }
                    } else {

                        Graph(
                            state.value.exerciseRecords,
                            "weight",
                            color = Color(state.value.exercise.muscleGroup.color),
                            state.value,
                            onEvent
                        ) { offset: DpOffset ->
                            var ofs: DpOffset = offset +
                                    DpOffset(0.dp, stableCardHeight.value) +
                                    DpOffset(0.dp, stableCardSubHeight.value)  +
                                    DpOffset(scaffoldPadding + 6.dp, scaffoldPadding + 2.dp)
                            if (ofs.x > screenWidth / 2) {
                                ofs = DpOffset(ofs.x - boxSizeX - 12.dp, ofs.y)
                            }
                            if (ofs.y > screenHeight / 2) {
                                ofs = DpOffset(ofs.x, ofs.y - boxSizeY - 24.dp)
                            }
                            pressOffset = ofs
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    if (state.value.exerciseRecords.size < 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.CenterVertically),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Add at least 2 records", textAlign = TextAlign.Center)
                        }
                    } else {
                        Graph(
                            state.value.exerciseRecords,
                            "reps",
                            color = Color(state.value.exercise.muscleGroup.color),
                            state.value,
                            onEvent
                        ) { offset: DpOffset ->

                            var ofs: DpOffset = offset + DpOffset(0.dp, stableItemHeight.value) +
                                    DpOffset(0.dp, stableCardHeight.value) +
                                    DpOffset(0.dp, stableCardSubHeight.value) +
                                    DpOffset(scaffoldPadding + 6.dp, scaffoldPadding + 2.dp)
                            if (ofs.x > screenWidth / 2) {
                                ofs = DpOffset(ofs.x - boxSizeX - 12.dp, ofs.y)
                            }
                            if (ofs.y > screenHeight / 2) {
                                ofs = DpOffset(ofs.x, ofs.y - boxSizeY - 24.dp)
                            }
                            pressOffset = ofs
                        }
                    }
                }


            }
            val alpha by animateFloatAsState(if (state.value.isBoxVisible) 1f else 0f)
            val scale by animateFloatAsState(if (state.value.isBoxVisible) 1f else 0f)

            Box(
                modifier = Modifier
                    .fillMaxSize()

            ) {
                Box(
                    modifier = Modifier
                        .offset(pressOffset.x, pressOffset.y)
                        .alpha(alpha)
                        .clip(RoundedCornerShape(20))
                        .background(color = MaterialTheme.colorScheme.background)
                        .graphicsLayer(
                            scaleX = 1f,
                            scaleY = scale
                        )
                        .border(
                            width = 1.dp, // Width of the border
                            color = MaterialTheme.colorScheme.onBackground, // Color of the border
                            shape = RoundedCornerShape(20) // Optional: You can specify the shape of the border
                        )

                ) {
                    Column(modifier = Modifier.padding(10.dp, 10.dp).onSizeChanged {
                        boxSizeX = with(density) { it.width.toDp() }
                        boxSizeY = with(density) { it.height.toDp() }
                    }) {


                            Row() {
                                if (state.value.selectedPoint >= 0 && state.value.selectedPoint < state.value.exerciseRecords.size) {
                                    Text("Weight: ${state.value.exerciseRecords[state.value.selectedPoint].weight}kg")
                                } else {
                                    Text("Weight")
                                }
                            }
                            Row() {
                                if (state.value.selectedPoint >= 0 && state.value.selectedPoint < state.value.exerciseRecords.size) {
                                    Text("Reps: ${state.value.exerciseRecords[state.value.selectedPoint].reps}")
                                } else {
                                    Text("Reps")
                                }
                            }
                            Row() {
                                if (state.value.selectedPoint >= 0 && state.value.selectedPoint < state.value.exerciseRecords.size) {
                                    Text(
                                        "Date: ${
                                            state.value.exerciseRecords[state.value.selectedPoint].date.format(
                                                dateTimeFormatterYear
                                            )
                                        }"
                                    )
                                } else {
                                    Text("Date")
                                }
                            }


                        Row(
                            horizontalArrangement = Arrangement.Center
                        ) {

                            Button(
                                onClick = {
                                    if (state.value.isBoxVisible) {
                                        onEvent(GraphEvent.DeleteRecord(state.value.exerciseRecords[state.value.selectedPoint]))
                                        onEvent(GraphEvent.SetBoxVisibility(false))
                                        Toast.makeText(context, "Exercise record deleted", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Text("Delete", color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                            Button(
                                onClick = {
                                },
                                modifier = Modifier.padding(start=10.dp)
                            ) {
                                Text("Save")
                            }
                        }
                    }

                }
            }
        }


    }
}