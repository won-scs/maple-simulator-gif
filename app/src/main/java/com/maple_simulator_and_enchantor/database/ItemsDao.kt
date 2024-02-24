package com.maple_simulator_and_enchantor.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter

@Dao
interface ItemsDao {
	@Query("SELECT * FROM items")
	suspend fun getAll(): List<Items>

	@Query("SELECT * FROM items WHERE id = :id")
	suspend fun getById(id: Int): List<Items>

	@Query("SELECT * FROM items WHERE wearCharacterId = :wearCharacterId")
	suspend fun getByWearCharacterId(wearCharacterId: Int): List<Items>

	@Query("SELECT * FROM items WHERE whereItem = :whereItem")
	suspend fun getByWhereItem(whereItem: Int): List<Items>

	@Query("DELETE FROM items WHERE id = :id")
	suspend fun deleteById(id: Int)

	@Query("UPDATE items SET extraStat = :extraStat, extraArmIgn = :extraArmIng WHERE id = :id")
	suspend fun updateExtraOption(extraStat: List<Int>, extraArmIng: Double, id: Int)

	@Query("UPDATE items SET starStat = :starStat, starArmIgn = :statArmIng, currentStar = :currentStar, maxStar = :maxStar WHERE id = :id")
	suspend fun updateStarStat(maxStar: Int, currentStar: Int, starStat: List<Int>, statArmIng: Double, id: Int)

	@Query("UPDATE items SET enchantStat = :enchantStat, remainUpGrade = :remainUpGrade, upGradeRestore = :upGradeRestore, goldHammer = :goldHammer, amazing = :amazing WHERE id = :id")
	suspend fun updateEnchant(remainUpGrade: Int, upGradeRestore: Int, goldHammer: Int, enchantStat: List<Int>, amazing: Int, id: Int)

	@Query("UPDATE items SET wearCharacterId = :wearCharacterId WHERE id = :id")
	suspend fun updateWearCharacterId(wearCharacterId: Int, id: Int)

	@Query("UPDATE items SET wearCharacterId = 0 WHERE wearCharacterId = :wearCharacterId")
	suspend fun updateResetWearCharacterId(wearCharacterId: Int)

	@Query("UPDATE items SET upAbilityList = :upAbility WHERE id = :id")
	suspend fun updateUpAbility(upAbility: List<Int>, id: Int)

	@Query("UPDATE items SET downAbilityList = :downAbility WHERE id = :id")
	suspend fun updateDownAbility(downAbility: List<Int>, id: Int)

	@Query("UPDATE items SET canStarForce = :canStarForce WHERE id = :id")
	suspend fun updateCanStarForce(canStarForce: Int, id: Int)

	@Query("UPDATE items SET useMesoInfo = :useMeso, useCashInfoList = :useCashList, destroyNum = :destroyNum WHERE id = :id")
	suspend fun updateUseInfo(useMeso: Long, useCashList: List<Int>, destroyNum: Int, id: Int)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(items: Items)

}