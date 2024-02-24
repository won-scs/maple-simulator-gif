package com.maple_simulator_and_enchantor.database

import android.os.FileObserver.DELETE
import android.provider.ContactsContract.CommonDataKinds.Nickname
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CharactersDao {
	@Query("SELECT * FROM characters")
	suspend fun getAll(): List<Characters>

	@Query("SELECT * FROM characters WHERE id = :characterId")
	suspend fun getByCharacterId(characterId: Int): List<Characters>

	@Query("SELECT * FROM characters WHERE nickname = :name")
	suspend fun getByNickname(name: String?): List<Characters>

	@Query("UPDATE characters SET pendant1Id = :pendant1Id, pendant2Id = :pendant2Id, " +
		"ring1Id = :ring1Id, ring2Id = :ring2Id, ring3Id = :ring3Id, ring4Id = :ring4Id  WHERE id = :id")
	suspend fun updatePendantRingId(pendant1Id: Int, pendant2Id: Int,
	ring1Id: Int, ring2Id: Int, ring3Id: Int, ring4Id: Int, id: Int)

	@Query("DELETE FROM characters WHERE nickname = :nickname")
	suspend fun deleteByNickname(nickname: String)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(character: Characters)

}