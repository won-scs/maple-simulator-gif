package com.maple_simulator_and_enchantor.database

import android.provider.SyncStateContract.Helpers.insert
import androidx.room.*

@Dao
interface ItemTableDao {
	@Query("SELECT * FROM itemTable WHERE itemCode = :itemCode")
	suspend fun getListByItemCode(itemCode: Int): List<ItemTable>

	@Query("SELECT * FROM itemTable WHERE name = :name")
	suspend fun getListByName(name: String): List<ItemTable>

	@Query("UPDATE itemTable SET canCreate = 1 WHERE name = :name")
	suspend fun updateCanCreate(name: String)

	@Query("SELECT * FROM itemTable")
	suspend fun getAll(): List<ItemTable>

	@Update
	suspend fun update(itemTable: ItemTable)

	suspend fun updateAll(itemTables: List<ItemTable>) {
		for(itemTable in itemTables) {
			update(itemTable)
		}
	}

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(itemTable: ItemTable)

	suspend fun insertAll(itemTables: List<ItemTable>) {
		for(itemTable in itemTables) {
			insert(itemTable)
		}
	}
}