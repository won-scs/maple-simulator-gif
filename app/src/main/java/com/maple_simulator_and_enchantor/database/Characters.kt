package com.maple_simulator_and_enchantor.database
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class Characters(
	@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
	@ColumnInfo(name = "jobName") val jobName: String,
	@ColumnInfo(name = "nickname") val nickname: String,
	@ColumnInfo(name = "characterLvl") val characterLvl: Int,
	@ColumnInfo(name = "baseStat") val baseStat: List<Int>,
	@ColumnInfo(name = "baseArmIgn") val baseArmIgn: Double,
	@ColumnInfo(name = "pendant1Id") val pendant1Id: Int,
	@ColumnInfo(name = "pendant2Id") val pendant2Id: Int,
	@ColumnInfo(name = "ring1Id") val ring1Id: Int,
	@ColumnInfo(name = "ring2Id") val ring2Id: Int,
	@ColumnInfo(name = "ring3Id") val ring3Id: Int,
	@ColumnInfo(name = "ring4Id") val ring4Id: Int,
	@ColumnInfo(name = "mainStat") val mainStat: List<Int>,
	@ColumnInfo(name = "subStat") val subStat: List<Int>,
) {
	fun getRingId(index: Int): Int {
		val ringIdList: List<Int> = listOf(0, ring1Id, ring2Id, ring3Id, ring4Id)
		return ringIdList[index]
	}
	fun getPendantId(index: Int): Int {
		val pendantIdList: List<Int> = listOf(0, pendant1Id, pendant2Id)
		return pendantIdList[index]
	}
}
