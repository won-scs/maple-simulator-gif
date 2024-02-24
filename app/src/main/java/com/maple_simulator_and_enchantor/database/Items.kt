package com.maple_simulator_and_enchantor.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Items (
	@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
	@ColumnInfo(name = "itemCode") val itemCode: Int,
	@ColumnInfo(name = "itemSetCode") val itemSetCode: Int,
	@ColumnInfo(name = "itemType") val itemType: Int,
	@ColumnInfo(name = "itemLvl") val itemLvl: Int,
	@ColumnInfo(name = "name") val name: String,
	@ColumnInfo(name = "wearCharacterId") val wearCharacterId: Int,
	@ColumnInfo(name = "whereItem", defaultValue = 0.toString()) val whereItem: Int,
	@ColumnInfo(name = "baseStat") val baseStat: List<Int>,
	@ColumnInfo(name = "baseArmIgn") val baseArmIgn: Double,
	@ColumnInfo(name = "extraStat") val extraStat: List<Int>,
	@ColumnInfo(name = "extraArmIgn") val extraArmIgn: Double,
	@ColumnInfo(name = "starStat") val starStat: List<Int>,
	@ColumnInfo(name = "starArmIgn") val starArmIgn: Double,
	@ColumnInfo(name = "enchantStat") val enchantStat: List<Int>,
	@ColumnInfo(name = "enchantArmIgn") val enchantArmIgn: Double,

	@ColumnInfo(name = "maxStar") val maxStar: Int,
	@ColumnInfo(name = "currentStar") val currentStar: Int,
	@ColumnInfo(name = "upGrade") val upGrade: Int,
	@ColumnInfo(name = "remainUpGrade") val remainUpGrade: Int,
	@ColumnInfo(name = "goldHammer") val goldHammer: Int,
	@ColumnInfo(name = "upGradeRestore") val upGradeRestore: Int,

	@ColumnInfo(name = "upAbilityList") var upAbilityList: List<Int>,
	@ColumnInfo(name = "downAbilityList") val downAbilityList: List<Int>,

	@ColumnInfo(name = "useMesoInfo") val useMesoInfo: Long,
	@ColumnInfo(name = "useCashInfoList") val useCashInfoList: List<Int>,
	@ColumnInfo(name = "destroyNum") val destroyNum: Int,

	@ColumnInfo(name = "canFlame") val canFlame: Int,
	@ColumnInfo(name = "canCube") val canCube: Int,
	@ColumnInfo(name = "canStarForce") val canStarForce: Int,
	@ColumnInfo(name = "amazing") val amazing: Int,
	@ColumnInfo(name = "canEnchant") val canEnchant: Int
	)
