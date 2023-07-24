package com.daykon.irontracker

import android.content.res.Configuration
import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.daykon.irontracker.db.Database
import com.daykon.irontracker.db.ExerciseRecord
import com.daykon.irontracker.db.GraphEvent
import com.daykon.irontracker.db.GraphViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalTextApi::class)
@Composable
fun Graph(
    records: List<ExerciseRecord>,
    mode: String = "reps", // Possible values ["reps", "weights"],
    color: Color = Color(0x00000000)
) {
    var minValue = 9999999f
    var maxValue = 0f
    var minDate: LocalDateTime = LocalDateTime.MAX
    var maxDate: LocalDateTime = LocalDateTime.MIN
    records.forEach { record ->
        if (record.date < minDate) {
            minDate = record.date
        }
        if (record.date > maxDate) {
            maxDate = record.date
        }
        val value = if (mode == "reps"){
            record.reps.toFloat()
        } else {
            record.weight
        }
        if (value < minValue){
            minValue = value
        }
        if (value > maxValue){
            maxValue = value
        }
    }
    val minutesBetween = ChronoUnit.MINUTES.between(minDate, maxDate)
    val dateTimeFormatterMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")
    val dateTimeFormatterYear: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")

    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        val textMeasurer = rememberTextMeasurer()
        val configuration = LocalConfiguration.current
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val screenHeight: Float = size.height
            val screenWidth: Float = size.width

            val paddingX0: Dp = 35.dp
            val paddingX1: Dp = 30.dp
            val paddingY0: Dp = 50.dp
            val paddingY1: Dp = 10.dp
            val xAxisSpace = (screenWidth - paddingX0.toPx() - paddingX1.toPx())
            val yAxisSpace = (screenHeight - paddingY0.toPx() - paddingY1.toPx())

            var minValueInt: Int = minValue.roundToInt()
            var maxValueInt: Int = maxValue.roundToInt()
            if (minValueInt == maxValueInt) {
                minValueInt -= 1
                maxValueInt += 1
            }
            var reduceMin = true
            val divisionValue = if (configuration.layoutDirection == Configuration.ORIENTATION_LANDSCAPE){
                200
            } else {
                100
            }
            val numMarksY: Int = (yAxisSpace / divisionValue).roundToInt()
            val numMarksX: Int = (xAxisSpace / 300).roundToInt()
            while((maxValueInt - minValueInt) % numMarksY != 0) {
                if (reduceMin && minValueInt > 0) {minValueInt -= 1}
                else if (!reduceMin) {maxValueInt += 1}
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


            /** placing x axis points */
            var j = 0
            while (j < numMarksX + 1) {
                val measuredText: TextLayoutResult = textMeasurer.measure(AnnotatedString(formatTime(j)))
                drawText(textMeasurer, formatTime(j),
                    Offset(xAxisSpace / numMarksX * (j) - measuredText.getLineRight(0) / 2 + paddingX0.toPx(),
                        screenHeight - 50),
                    style = TextStyle.Default.copy(color = Color.White)
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
                    Offset(0f,
                        screenHeight - yAxisSpace / numMarksY * (j) -
                            measuredText.getLineBottom(0) - paddingY0.toPx()),
                    style = TextStyle.Default.copy(color = Color.White)
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
                Offset(xAxisSpace - measuredText.getLineRight(0) + paddingX0.toPx() ,
                    yAxisSpace - paddingY1.toPx()),
                style = TextStyle.Default.copy(color = Color.White)
            )

            fun getXYValuesRecord(record: ExerciseRecord): Array<Float>{
                val date: Long = ChronoUnit.MINUTES.between(minDate, record.date)
                val value = if (mode == "reps"){
                    record.reps.toFloat()
                } else {
                    record.weight
                }
                val x1: Float = xAxisSpace * date / minutesBetween + paddingX0.toPx()
                Log.d("DEBUG", minValueInt.toString() + " " + maxValueInt.toString() + " " + value.toString() + " | " + ((value - minValueInt) / (maxValueInt - minValueInt)).toString())
                var y1: Float = yAxisSpace * (1 - (value - minValueInt) / (maxValueInt - minValueInt))
                if (configuration.layoutDirection == Configuration.ORIENTATION_LANDSCAPE){
                    y1 = yAxisSpace - y1
                }

                return arrayOf(x1, y1)
            }

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
                val pos: Array<Float> = getXYValuesRecord(records[i])
                val x1: Float = pos[0]
                val y1: Float = pos[1]

                coordinates.add(PointF(x1,y1))
                /** drawing circles to indicate all the points */
                drawCircle(
                    color = color,
                    radius = 5f,
                    center = Offset(x1,y1)
                )
            }

            val controlPoints1: ArrayList<PointF> = ArrayList()
            val controlPoints2: ArrayList<PointF> = ArrayList()

            for (i in 1 until coordinates.size) {
                controlPoints1.add(PointF((coordinates[i].x + coordinates[i - 1].x) / 2, coordinates[i - 1].y))
                controlPoints2.add(PointF((coordinates[i].x + coordinates[i - 1].x) / 2, coordinates[i].y))
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
                        lineTo(getXYValuesRecord(records[records.size - 1])[0], size.height - paddingY0.toPx())
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
                    color = Color.White,
                    style = Stroke(
                        width = 5f,
                        cap = StrokeCap.Square

                    )
                )

            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun GraphScreen (
    db: Database,
    exerciseId: String = "0"
    ) {
    val graphViewModel = GraphViewModel(db.exerciseDao,db.exerciseRecordDao, exerciseId.toInt())
    val onEvent = graphViewModel::onEvent
    val state = graphViewModel.state.collectAsState()

    Log.d("TESTDEBUG", "exercise_id$exerciseId")
    onEvent(GraphEvent.SetExerciseId(exerciseId.toInt()))
    Scaffold { padding ->
        Column(modifier = Modifier.padding(PaddingValues(8.dp, 8.dp, 8.dp, 0.dp))){

            Row {
                ElevatedCard(modifier = Modifier.wrapContentSize(),

                             shape= CutCornerShape(15,0,15,0),
                             colors=CardDefaults.cardColors(containerColor = Color(state.value.exercise.muscleGroup.color))) {
                    Box (contentAlignment = Alignment.Center,
                    modifier = Modifier.wrapContentSize().fillMaxWidth()){
                        Text(state.value.exercise.exercise.name,
                             style = MaterialTheme.typography.headlineSmall,
                             modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
                             color = MaterialTheme.colorScheme.background,
                             textAlign = TextAlign.Center)
                    }
                }

            }
            Row(horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(PaddingValues(0.dp, 8.dp, 0.dp,0.dp))) {
                Box(contentAlignment = Alignment.Center) {
                    Text(state.value.exercise.muscleGroup.name)
                }
            }
            Row(modifier = Modifier.fillMaxSize().weight(1f)) {
                if (state.value.exerciseRecords.size < 2){
                    Box (modifier = Modifier.fillMaxSize().align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                    ) {
                        Text("Weight: Not enough records yet", textAlign = TextAlign.Center)
                    }
                } else {

                    Graph(state.value.exerciseRecords, "weight", color = Color(state.value.exercise.muscleGroup.color))
                }
            }
            Row(modifier = Modifier.fillMaxSize().weight(1f)) {
                if (state.value.exerciseRecords.size < 2){
                    Box (modifier = Modifier.fillMaxSize().align(Alignment.CenterVertically),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Reps: Not enough records yet", textAlign = TextAlign.Center)
                    }
                } else {

                    Graph(state.value.exerciseRecords, "reps", color = Color(state.value.exercise.muscleGroup.color))
                }
            }
        }


    }
}