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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CountDownTimerViewModel : ViewModel() {

    private val countDownTimer: CustomizedCountDownTimer by lazy {
        CustomizedCountDownTimer(1000, ::onTimerTicked, ::onTimerFinished)
    }

    private val timerFormatRegex = "^(\\d{2})(\\d{2})(\\d{2})".toRegex()
    private val timerFormatPattern = "$1h: $2m: $3s"

    private var unformattedTimerValue: String = ""
        set(value) {
            field = if (value == "-") {
                field.dropLast(1)
            } else {
                "$field$value"
            }
        }
        get() {
            return if (field.length <6) {
                field.padStart(6, '0')
            } else {
                field
            }
        }

    var timerValueForDisplay by mutableStateOf("00h: 00m: 00s")
        private set

    var correctedTimerValueForDisplay by mutableStateOf("00h: 00m: 00s")
        private set

    var sweepAngle by mutableStateOf(0f)
        private set

    var timerStartValue by mutableStateOf(0L)
        private set

    var timerState by mutableStateOf(INVALID)
        private set

    fun onKeyPadPressed(keyValue: String) {
        if ((keyValue == "0" && unformattedTimerValue.isEmpty()) || (unformattedTimerValue.length == 6 && unformattedTimerValue[0] != '0')) return

        unformattedTimerValue = keyValue
        updateFormattedTimerValue()
    }

    fun onBackSpacePressed() {
        if (unformattedTimerValue.isEmpty()) return
        unformattedTimerValue = "-" // This will drop the last character
        updateFormattedTimerValue()
    }

    fun startCountDown() {
        val groups = unformattedTimerValue.chunked(2)
        val hours: Long = groups[0].toLong() * 60 * 60
        val min: Long = groups[1].toLong() * 60
        val seconds: Long = groups[2].toLong()
        timerStartValue = hours + min + seconds
        timerState = STARTED
        countDownTimer.start(timerStartValue * 1000)
    }

    fun pauseTimer() {
        timerState = PAUSED
        countDownTimer.pause()
    }

    fun resumeTimer() {
        timerState = STARTED
        countDownTimer.resume()
    }

    fun restartTimer() {
        timerState = STARTED
        countDownTimer.cancel()
        countDownTimer.restart()
    }

    fun deleteTimer() {
        timerState = INVALID
        countDownTimer.cancel()
        timerStartValue = 0L
    }

    private fun onTimerTicked(timeLeft: Long) {
        val totalSeconds = timeLeft / 1000
        val hour = (totalSeconds / 3600).toString().zeroPad(2)
        val min = ((totalSeconds % 3600) / 60).toString().zeroPad(2)
        val sec = ((totalSeconds % 3600) % 60).toString().zeroPad(2)

        correctedTimerValueForDisplay = "$hour$min$sec".replace(timerFormatRegex, timerFormatPattern)

        sweepAngle = (1f - totalSeconds.toFloat() / (timerStartValue).toFloat()) * 360f

        if (sweepAngle == 360f) {
            sweepAngle -= 0.1f
        }
        // Log.i("TIMER", correctedTimerValueForDisplay)
        // Log.i("TIMER", "$timeLeft $timerStartValue $sweepAngle")
    }

    private fun onTimerFinished() {
        timerState = FINISHED
    }

    private fun updateFormattedTimerValue() {
        timerValueForDisplay = unformattedTimerValue.replace(timerFormatRegex, timerFormatPattern)
    }

    private fun String.zeroPad(charCount: Int): String {
        return if (this.length >= charCount) {
            this
        } else {
            this.padStart(charCount, '0')
        }
    }
}
