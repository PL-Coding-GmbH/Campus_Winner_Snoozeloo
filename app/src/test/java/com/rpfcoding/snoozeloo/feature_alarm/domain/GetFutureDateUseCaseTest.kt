package com.rpfcoding.snoozeloo.feature_alarm.domain

import org.junit.Assert
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class GetFutureDateUseCaseTest {

    private lateinit var getFutureDateUseCase: GetFutureDateUseCase

    @Test
    fun `test repeat days are Thursday and Friday but today is Monday`() {
        getFutureDateUseCase = GetFutureDateUseCase(
            now = LocalDateTime.of(2024, 11, 11, 7, 0 ,0)
        )

        val repeatDays = setOf(DayValue.THURSDAY, DayValue.FRIDAY)

        val futureDateTime = getFutureDateUseCase(10, 30, repeatDays)

        Assert.assertEquals(DayOfWeek.THURSDAY, futureDateTime.dayOfWeek)
        Assert.assertEquals(14, futureDateTime.dayOfMonth)
        Assert.assertEquals(10, futureDateTime.hour)
        Assert.assertEquals(30, futureDateTime.minute)
    }

    @Test
    fun `test repeat days are Thursday and Friday but today is Wednesday`() {
        getFutureDateUseCase = GetFutureDateUseCase(
            now = LocalDateTime.of(2024, 11, 13, 11, 0 ,0)
        )

        val repeatDays = setOf(DayValue.THURSDAY, DayValue.FRIDAY)

        val futureDateTime = getFutureDateUseCase(10, 30, repeatDays)

        Assert.assertEquals(DayOfWeek.THURSDAY, futureDateTime.dayOfWeek)
        Assert.assertEquals(14, futureDateTime.dayOfMonth)
        Assert.assertEquals(10, futureDateTime.hour)
        Assert.assertEquals(30, futureDateTime.minute)
    }

    @Test
    fun `test repeat day is Friday, Today is Friday and the set alarm is earlier than the current time, Get Friday next week`() {
        getFutureDateUseCase = GetFutureDateUseCase(
            now = LocalDateTime.of(2024, 11, 15, 11, 0 ,0)
        )

        val repeatDays = setOf(DayValue.FRIDAY)

        val futureDateTime = getFutureDateUseCase(10, 30, repeatDays)

        Assert.assertEquals(DayOfWeek.FRIDAY, futureDateTime.dayOfWeek)
        Assert.assertEquals(22, futureDateTime.dayOfMonth)
        Assert.assertEquals(10, futureDateTime.hour)
        Assert.assertEquals(30, futureDateTime.minute)
    }
}