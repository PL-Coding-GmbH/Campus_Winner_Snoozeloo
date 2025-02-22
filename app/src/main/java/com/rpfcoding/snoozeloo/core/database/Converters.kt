package com.rpfcoding.snoozeloo.core.database

import androidx.room.TypeConverter

class Converters {

    // GENERAL FEEDBACK: Careful with such type converters
    @TypeConverter
    fun setToString(value: Set<Int>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun stringToSet(value: String): Set<Int> {
        return value.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }
}