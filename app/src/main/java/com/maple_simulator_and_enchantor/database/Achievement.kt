package com.maple_simulator_and_enchantor.database
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement")
data class Achievement(
	@PrimaryKey @ColumnInfo(name = "name") val name: String,
	@ColumnInfo(name = "maxProgress") val maxProgress: Int,
	@ColumnInfo(name = "currentProgress") val currentProgress: Int,
) {
}
