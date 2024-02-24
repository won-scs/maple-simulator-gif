package com.maple_simulator_and_enchantor.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson

class IntegerListTypeConverter {
	@TypeConverter
	fun listToJson(value: List<Int>): String? {
		return Gson().toJson(value)
	}
	@TypeConverter
	fun jsonToList(value: String): List<Int>? {
		return Gson().fromJson(value, Array<Int>::class.java)?.toList()
	}
}