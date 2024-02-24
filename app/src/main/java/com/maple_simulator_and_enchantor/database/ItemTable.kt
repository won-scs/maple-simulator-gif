package com.maple_simulator_and_enchantor.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itemTable")
data class ItemTable (
	@PrimaryKey @ColumnInfo(name = "itemCode") val itemCode: Int,
  @ColumnInfo(name = "itemSetCode") val itemSetCode: Int,
	@ColumnInfo(name = "itemType") val itemType: Int,
	@ColumnInfo(name = "itemLvl") val itemLvl: Int,
	@ColumnInfo(name = "name") val name: String,
	@ColumnInfo(name = "baseStat") val baseStat: List<Int>,
	@ColumnInfo(name = "baseArmIgn") val baseArmIgn: Double,
	@ColumnInfo(name = "upGrade") val upGrade: Int,
	@ColumnInfo(name = "canCube") val canCube: Int,
	@ColumnInfo(name = "canStarForce") val canStarForce: Int,
	@ColumnInfo(name = "maxStar") val maxStar: Int,
	@ColumnInfo(name = "canFlame") val canFlame: Int,
	@ColumnInfo(name = "canEnchant") val canEnchant: Int,
	@ColumnInfo(name = "canCreate") val canCreate: Int
)

