package com.rpfcoding.snoozeloo.feature_alarm.scheduler_receiver

import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.rpfcoding.snoozeloo.core.domain.ringtone.ALARM_MAX_REMINDER_MILLIS
import com.rpfcoding.snoozeloo.core.presentation.designsystem.SnoozelooTheme
import com.rpfcoding.snoozeloo.core.util.hideNotification
import com.rpfcoding.snoozeloo.core.util.isOreoMr1Plus
import com.rpfcoding.snoozeloo.core.util.isOreoPlus
import com.rpfcoding.snoozeloo.feature_alarm.domain.Alarm
import com.rpfcoding.snoozeloo.feature_alarm.domain.AlarmConstants
import com.rpfcoding.snoozeloo.feature_alarm.domain.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.rpfcoding.snoozeloo.core.domain.ringtone.RingtoneManager as MyRingtoneManager

class ReminderActivity : ComponentActivity() {

    private val viewModel: ReminderViewModel by viewModel()
    private val ringtoneManager: MyRingtoneManager by inject()
    private val alarmScheduler: AlarmScheduler by inject()
    private val scope: CoroutineScope by inject()
    private val vibrator by lazy {
        getSystemService(Vibrator::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        showOverLockscreen()
        val alarmId = intent?.getStringExtra(AlarmConstants.EXTRA_ALARM_ID) ?: throw Exception("Alarm ID is not found.")

        setContent {
            SnoozelooTheme {
                var effectsSet by remember { mutableStateOf(true) }

                // FEEDBACK: Unclear effect to me, could need a why-comment
                LaunchedEffect(Unit) {
                    viewModel.getAlarmById(alarmId)
                    delay(500L)
                    effectsSet = false
                }

                // FEEDBACK: Could be moved to VM, config change resets the delay
                LaunchedEffect(Unit) {
                    delay(ALARM_MAX_REMINDER_MILLIS)
                    viewModel.alarm?.let {
                        disableAlarmAndFinish(it, it.isOneTime, shouldSnooze = true)
                    }
                }

                LaunchedEffect(viewModel.alarm, effectsSet) {
                    if (!effectsSet && viewModel.alarm != null) {
                        effectsSet = true
                        setupEffects(viewModel.alarm!!)
                    }
                }

                viewModel.alarm?.let { alarm ->
                    AlarmTriggerScreen(
                        alarm = viewModel.alarm!!,
                        onTurnOffClick = {
                            disableAlarmAndFinish(alarm, alarm.isOneTime)
                        },
                        onSnoozeClick = {
                            disableAlarmAndFinish(alarm, alarm.isOneTime, shouldSnooze = true)
                        }
                    )
                }
            }
        }
    }

    private fun disableAlarmAndFinish(alarm: Alarm, isOneTime: Boolean, shouldSnooze: Boolean = false) {
        if (shouldSnooze) {
            alarmScheduler.schedule(
                alarm = alarm,
                shouldSnooze = true
            )
        } else {
            if (isOneTime) {
                viewModel.disableAlarm(alarm.id)
            } else {
                viewModel.rescheduleAlarm()
            }
        }
        hideNotification(alarm.id.hashCode())
        ringtoneManager.stop()
        vibrator.cancel()
        finish()
    }

    // FEEDBACK: Would move such functions to the data layer
    private fun setupEffects(alarm: Alarm) {
        val pattern = AlarmConstants.VIBRATE_PATTERN_LONG_ARR
        if (isOreoPlus() && alarm.vibrate) {
            scope.launch {
                delay(500L)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
            }
        }

        val ringtoneUri = alarm.ringtoneUri.let {
            if (it.isNotBlank()) {
                return@let Uri.parse(it)
            }

            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        val volume = (alarm.volume / 100f)
        if (ringtoneUri != null && !ringtoneManager.isPlaying()) {
            ringtoneManager.play(uri = ringtoneUri.toString(), isLooping = true, volume = volume)
        }
    }

    private fun showOverLockscreen() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        if (isOreoMr1Plus()) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }
}