package com.maple_simulator_and_enchantor.database

import android.os.FileObserver.DELETE
import android.provider.ContactsContract.CommonDataKinds.Nickname
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AchievementDao {
	@Query("SELECT * FROM achievement")
	suspend fun getAll(): List<Achievement>

	@Query("SELECT * FROM achievement WHERE name = :name")
	suspend fun getByName(name: String): List<Achievement>

	@Query("UPDATE achievement SET maxProgress = :maxProgress, " +
		"currentProgress = :currentProgress WHERE name = :name")
	suspend fun updateProgress(name: String, maxProgress: Int, currentProgress: Int)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(achievement: Achievement)

}