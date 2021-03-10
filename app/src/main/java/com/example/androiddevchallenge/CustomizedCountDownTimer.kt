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

import android.os.CountDownTimer
import kotlin.properties.Delegates

const val INVALID = 0
const val STARTED = 1
const val PAUSED = 2
const val CANCELLED = 3
const val FINISHED = 4

class CustomizedCountDownTimer(val countDownInterval: Long = 1000, val onTicked: (Long) -> Unit, val onFinished: () -> Unit) {

    private var timerStartValue by Delegates.notNull<Long>()
    private var countDownTimer: CountDownTimer? = null
    private var currentTimerStartValue: Long = 0L
    var state: Int = INVALID

    private fun initializeAndStart(startValue: Long) {
        state = STARTED
        countDownTimer = object : CountDownTimer(startValue, countDownInterval) {
            override fun onTick(timeLeft: Long) {
                currentTimerStartValue = timeLeft
                onTicked.invoke(timeLeft)
            }

            override fun onFinish() {
                if (state == STARTED) {
                    state = FINISHED
                    onFinished()
                }
            }
        }.start()
    }

    fun start(startValue: Long) {
        timerStartValue = startValue
        initializeAndStart(timerStartValue)
    }

    fun pause() {
        if (state != STARTED) return
        state = PAUSED
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun resume() {
        if (currentTimerStartValue == 0L) return
        initializeAndStart(currentTimerStartValue)
    }

    fun restart() {
        if (timerStartValue == 0L) return
        initializeAndStart(timerStartValue)
    }

    fun cancel() {
        state = CANCELLED
        countDownTimer?.cancel()
        countDownTimer = null
        currentTimerStartValue = 0L
    }
}
