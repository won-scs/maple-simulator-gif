package com.maple_simulator_and_enchantor.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "characterTable")
data class CharacterTable(
	@PrimaryKey @ColumnInfo(name = "jobName") val jobName: String,
	@ColumnInfo(name = "baseStat") val baseStat: List<Int>,
	@ColumnInfo(name = "baseArmIgn") val baseArmIgn: Double,
	@ColumnInfo(name = "mainStat") val mainStat: List<Int>,
	@ColumnInfo(name = "subStat") val subStat: List<Int>,
) {
}