package com.maple_simulator_and_enchantor.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CubeTableDao {
	@Query("SELECT * FROM cubeTable")
	suspend fun getAll(): List<CubeTable>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(cubeTable: CubeTable)

	suspend fun insertAll(cubeTables : List<CubeTable>) {
		for(cubeTable in cubeTables) {
			insert(cubeTable)
		}
	}
}