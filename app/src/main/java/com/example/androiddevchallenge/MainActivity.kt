/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.ui.theme.MyTheme

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<CountDownTimerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp(
                    timerStartValue = viewModel.timerStartValue,
                    onStartValueSet = viewModel::startCountDown,
                    instantTimerValueForDisplay = viewModel.timerValueForDisplay,
                    correctedTimerValueForDisplay = viewModel.correctedTimerValueForDisplay,
                    sweepAngle = viewModel.sweepAngle,
                    keypadPressed = viewModel::onKeyPadPressed,
                    backSpacePressed = viewModel::onBackSpacePressed,
                    timerState = viewModel.timerState,
                    pauseTimer = viewModel::pauseTimer,
                    resumeTimer = viewModel::resumeTimer,
                    restartTimer = viewModel::restartTimer,
                    deleteTimer = viewModel::deleteTimer
                )
            }
        }
    }
}

// Start building your app here!
@Composable
fun MyApp(
    timerStartValue: Long,
    onStartValueSet: () -> Unit,
    instantTimerValueForDisplay: String,
    correctedTimerValueForDisplay: String,
    sweepAngle: Float,
    keypadPressed: (String) -> Unit,
    backSpacePressed: () -> Unit,
    timerState: Int,
    pauseTimer: () -> Unit,
    resumeTimer: () -> Unit,
    restartTimer: () -> Unit,
    deleteTimer: () -> Unit
) {
    Surface(color = MaterialTheme.colors.background) {
        Scaffold {
            if (timerStartValue == 0L) {
                StartTimerEntry(onStartValueSet, instantTimerValueForDisplay, keypadPressed, backSpacePressed)
            } else {
                StartTimerCountDownScreen(sweepAngle, correctedTimerValueForDisplay, timerState, pauseTimer, resumeTimer, restartTimer, deleteTimer)
            }
        }
    }
}

@Composable
fun StartTimerCountDownScreen(
    sweepAngle: Float,
    instantTimerValueForDisplay: String,
    currentState: Int,
    pauseTimer: () -> Unit,
    resumeTimer: () -> Unit,
    restartTimer: () -> Unit,
    deleteTimer: () -> Unit
) {

    // val (animationStarted, setAnimationStarted) = remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 24.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = instantTimerValueForDisplay,
                fontWeight = FontWeight.Thin,
                fontSize = 40.sp
            )

            val sweep: Float by animateFloatAsState(targetValue = sweepAngle)
            CustomCountDownIndicator(
                sweep,
                modifier = Modifier
                    .width(280.dp)
                    .height(280.dp)
                    .padding(top = 24.dp, bottom = 24.dp)
            )
        }

        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.label_restart),
                modifier = Modifier
                    .padding(end = 34.dp)
                    .clickable {
                        restartTimer()
                    }
            )
            when (currentState) {
                // Paused
                PAUSED -> {
                    Button(
                        modifier = Modifier.size(80.dp), shape = CircleShape,
                        onClick = { resumeTimer() }
                    ) {
                        Icon(Icons.Outlined.PlayArrow, "Timer Action")
                    }
                }

                // In Progress
                STARTED -> {
                    Button(
                        modifier = Modifier.size(80.dp), shape = CircleShape,
                        onClick = { pauseTimer() }
                    ) {
                        Icon(Icons.Outlined.Pause, "Timer Action")
                    }
                }

                // Finished
                FINISHED -> {
                    Button(
                        modifier = Modifier.size(80.dp), shape = CircleShape,
                        onClick = { restartTimer() }
                    ) {
                        Icon(Icons.Outlined.Restore, "Timer Action")
                    }
                }
            }
            Text(
                text = stringResource(R.string.label_cancel),
                modifier = Modifier
                    .padding(start = 34.dp)
                    .clickable {
                        deleteTimer()
                    }
            )
        }
    }
}

@Composable
fun CustomCountDownIndicator(
    sweepAngle: Float,
    modifier: Modifier = Modifier,
    pathWidth: Float = 25f,
    pathColor: Color = MaterialTheme.colors.primary,
    innerColor: Color = MaterialTheme.colors.primaryVariant
) {

    Canvas(modifier = modifier) {

        // Circle to show the path to trace
        drawCircle(
            color = Color.LightGray,
            style = Stroke(pathWidth, 3f, StrokeCap.Butt, StrokeJoin.Miter, null),
            center = Offset(x = size.width / 2, y = size.height / 2),
            radius = size.minDimension / 2,
        )

        // This shows the progress

        val progressPath = Path()
        progressPath.arcTo(Rect(Offset(x = size.width / 2, y = size.height / 2), size.minDimension / 2), 270f, sweepAngle, forceMoveTo = true)
        drawPath(
            path = progressPath,
            color = pathColor,
            style = Stroke(pathWidth, 3f, StrokeCap.Round, StrokeJoin.Miter, null)
        )

        drawCircle(
            color = innerColor,
            center = Offset(x = size.width / 2, y = size.height / 2),
            radius = (size.minDimension / 2 - pathWidth) * sweepAngle / 360
        )

        // Find end point
        val coordinates = floatArrayOf(0f, 0f)
        val pm = android.graphics.PathMeasure(progressPath.asAndroidPath(), false)

        if (pm.getPosTan(pm.length, coordinates, null)) {
            // This shows the endpoint
            drawCircle(
                color = Color.White,
                center = Offset(x = coordinates[0], y = coordinates[1]),
                radius = pathWidth + 18f
            )

            drawCircle(
                color = pathColor,
                center = Offset(x = coordinates[0], y = coordinates[1]),
                radius = pathWidth + 3f
            )

            // This is stroke around the endpoint
            drawCircle(
                color = pathColor,
                style = Stroke(2f, 3f, StrokeCap.Round, StrokeJoin.Miter, null),
                center = Offset(x = coordinates[0], y = coordinates[1]),
                radius = pathWidth + 14f
            )
        }
    }
}

@Composable
fun StartTimerEntry(
    onStartValueSet: () -> Unit,
    instantTimerValueForDisplay: String,
    keypadPressed: (String) -> Unit,
    backSpacePressed: () -> Unit
) {
    // val (timerValue, onTimerValueChange) = remember{ mutableStateOf("0")}

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = instantTimerValueForDisplay,
                    fontWeight = FontWeight.Thin,
                    fontSize = 40.sp
                )
                IconButton(
                    onClick = { backSpacePressed() }
                ) {
                    Icon(Icons.Outlined.Backspace, "Clear value entered")
                }
            }
            OnScreenNumericKeyPad(keypadPressed)
        }
        Row(modifier = Modifier.padding(24.dp)) {

            Button(
                modifier = Modifier.size(80.dp), shape = CircleShape,
                onClick = { onStartValueSet.invoke() }
            ) {
                Icon(Icons.Outlined.PlayArrow, "Timer Action")
            }
        }
    }
}

@Composable
fun OnScreenNumericKeyPad(keypadPressed: (String) -> Unit) {
    Column {
        OnScreenKeyPadRow("1", "2", "3", keypadPressed)
        OnScreenKeyPadRow("4", "5", "6", keypadPressed)
        OnScreenKeyPadRow("7", "8", "9", keypadPressed)
        OnScreenKeyPadRow("", "0", "", keypadPressed)
    }
}

@Composable
fun OnScreenKeyPadRow(
    left: String,
    mid: String,
    right: String,
    keypadPressed: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        NumberKey(left, keypadPressed)
        NumberKey(mid, keypadPressed)
        NumberKey(right, keypadPressed)
    }
}

@Composable
fun NumberKey(
    value: String,
    keypadPressed: (String) -> Unit
) {
    if (value.isEmpty()) return
    Button(
        modifier = Modifier
            .size(100.dp)
            .padding(10.dp),
        shape = CircleShape,
        onClick = {
            keypadPressed(value)
        }
    ) {
        Text(text = value, fontWeight = FontWeight.ExtraLight, fontSize = 30.sp)
    }
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp(
            0L,
            {},
            "00:00:00",
            "00:80:98",
            0f,
            {},
            {},
            STARTED,
            { },
            { },
            { },
            { }
        )
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp(
            200L,
            {},
            "00:80:98",
            "00:80:98",
            359f,
            {},
            {},
            FINISHED,
            { },
            { },
            { },
            { }
        )
    }
}
