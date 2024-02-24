package com.maple_simulator_and_enchantor.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cubeTable")
data class CubeTable(
	@PrimaryKey @ColumnInfo(name = "tableName") val tableName: String,
	@ColumnInfo(name = "optionList") val optionList: List<Int>
) {
}
