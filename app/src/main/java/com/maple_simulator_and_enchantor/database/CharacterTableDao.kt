package com.maple_simulator_and_enchantor.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CharacterTableDao {
	@Query("SELECT * FROM characterTable WHERE jobName = :name")
	suspend fun getByJobName(name: String?): List<CharacterTable>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(characterTable: CharacterTable)

	suspend fun insertAll(characterTables : List<CharacterTable>) {
		for(characterTable in characterTables) {
			insert(characterTable)
		}
	}
}