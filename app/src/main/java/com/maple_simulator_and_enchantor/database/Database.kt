package com.maple_simulator_and_enchantor.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.internal.synchronized

//@androidx.room.Database(entities = [ItemTable::class, Items::class, Characters::class, CubeTable::class], version = 1)
//@TypeConverters(IntegerListTypeConverter::class)
//abstract class Database: RoomDatabase() {
//	abstract fun itemTableDao(): ItemTableDao
//	abstract fun charactersDao(): CharactersDao
//	abstract fun cubeTableDao(): CubeTableDao
//	abstract fun itemsDao(): ItemsDao
//
//	companion object {
//		private var instance: Database? = null
//
//		@OptIn(InternalCoroutinesApi::class)
//		@Synchronized
//		fun getInstance(context: Context): Database? {
//			if(instance == null) {
//				synchronized(Database::class) {
//					instance = Room.databaseBuilder(
//						context.applicationContext,
//						Database::class.java,
//						"user.db")
//						.build()
//				}
//			}
//			return instance
//		}
//	}
//}
@androidx.room.Database(entities = [ItemTable::class,
	Items::class, Characters::class, CubeTable::class,
																	 CharacterTable::class], version = 27)
@TypeConverters(IntegerListTypeConverter::class)
abstract class Database: RoomDatabase() {
	abstract fun itemTableDao(): ItemTableDao
	abstract fun charactersDao(): CharactersDao
	abstract fun cubeTableDao(): CubeTableDao
	abstract fun itemsDao(): ItemsDao
	abstract fun characterTableDao(): CharacterTableDao

	companion object {
		private val MIGRATION_1_2 = object : Migration(1, 2) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("ALTER TABLE items ADD COLUMN whereItem INTEGER NOT NULL default 0")
			}
		}
		private val MIGRATION_2_3 = object : Migration(2, 3) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("ALTER TABLE items ADD COLUMN remainUpGrade INTEGER NOT NULL default 0")
			}
		}
		private val MIGRATION_3_4 = object : Migration(3, 4) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("ALTER TABLE characters ADD COLUMN mainStat TEXT NOT NULL default '[0]'")
				database.execSQL("ALTER TABLE characters ADD COLUMN subStat TEXT NOT NULL default '[2]'")
				database.execSQL("CREATE TABLE IF NOT EXISTS 'characterTable' " +
					"('jobName' TEXT Not NULL,  " +
					"'baseStat' TEXT Not NULL, " +
					"'baseArmIgn' Double Not NULL," +
					"'mainStat' TEXT NOT NULL," +
					"'subStat' TEXT NOT NULL, PRIMARY KEY('jobName'))")
			}
		}
		private val MIGRATION_4_5 = object : Migration(4, 5) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("UPDATE items SET itemCode = 480008 WHERE itemCode = 540008")
				database.execSQL("UPDATE items SET itemCode = 480449 WHERE itemCode = 540449")
				database.execSQL("UPDATE items SET itemCode = 480499 WHERE itemCode = 540499")
			}
		}
		private val MIGRATION_5_6 = object : Migration(5, 6) {
			override fun migrate(database: SupportSQLiteDatabase) {
			}
		}
		private val MIGRATION_6_7 = object : Migration(6, 7) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("UPDATE items SET wearCharacterId = 0")
				database.execSQL("DELETE FROM characters WHERE jobName = '아델'")
			}
		}
		private val MIGRATION_7_8 = object : Migration(7, 8) {
			override fun migrate(database: SupportSQLiteDatabase) {
			}
		}
		private val MIGRATION_8_9 = object : Migration(8, 9) {
			override fun migrate(database: SupportSQLiteDatabase) {
			}
		}
		private val MIGRATION_9_10 = object : Migration(9, 10) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("ALTER TABLE items ADD COLUMN itemSetCode INTEGER NOT NULL default 0")
				database.execSQL("UPDATE items SET itemSetCode = 103 WHERE name = '에테르넬 나이트헬름'")
				database.execSQL("UPDATE items SET itemSetCode = 103 WHERE name = '에테르넬 나이트아머'")
				database.execSQL("UPDATE items SET itemSetCode = 103 WHERE name = '에테르넬 나이트팬츠'")
				database.execSQL("UPDATE items SET itemSetCode = 102 WHERE name = '아케인셰이드 나이트케이프'")
				database.execSQL("UPDATE items SET itemSetCode = 102 WHERE name = '아케인셰이드 나이트글러브'")
				database.execSQL("UPDATE items SET itemSetCode = 102 WHERE name = '아케인셰이드 나이트슈즈'")
				database.execSQL("UPDATE items SET itemSetCode = 102 WHERE name = '아케인셰이드 나이트숄더'")
				database.execSQL("UPDATE items SET itemSetCode = 604 WHERE name = '마력이 깃든 안대'")
				database.execSQL("UPDATE items SET itemSetCode = 604 WHERE name = '루즈 컨트롤 머신 마크'")
				database.execSQL("UPDATE items SET itemSetCode = 604 WHERE name = '몽환의 벨트'")
				database.execSQL("UPDATE items SET itemSetCode = 604 WHERE name = '거대한 공포'")
				database.execSQL("UPDATE items SET itemSetCode = 604 WHERE name = '창세의 뱃지'")
				database.execSQL("UPDATE items SET itemSetCode = 604 WHERE name = '고통의 근원'")
				database.execSQL("UPDATE items SET itemSetCode = 604 WHERE name = '저주받은 적의 마도서'")
				database.execSQL("UPDATE items SET itemSetCode = 604 WHERE name = '미트라의 분노 : 전사'")
				database.execSQL("UPDATE items SET itemSetCode = 604 WHERE name = '커맨더 포스 이어링'")
				database.execSQL("UPDATE items SET itemSetCode = 606 WHERE name = '데이브레이크 펜던트'")
				database.execSQL("UPDATE items SET itemSetCode = 606 WHERE name = '여명의 가디언 엔젤 링'")
			}
		}
		private val MIGRATION_10_11 = object : Migration(10, 11) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("UPDATE items SET itemSetCode = 777 WHERE name = '제네시스 투핸드소드'")
			}
		}
		private val MIGRATION_11_12 = object : Migration(11, 12) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("UPDATE items SET maxStar = 20 WHERE name = '스칼렛 링'")
			}
		}
		private val MIGRATION_12_13 = object : Migration(12, 13) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("UPDATE items SET maxStar = 20 WHERE name = '스칼렛 링'")
			}
		}
		private val MIGRATION_13_14 = object : Migration(13, 14) { override fun migrate(database: SupportSQLiteDatabase) {} }
		private val MIGRATION_14_15 = object : Migration(14, 15) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("UPDATE items SET itemSetCode = 403 WHERE name = '에테르넬 시프반다나'")
				database.execSQL("UPDATE items SET itemSetCode = 403 WHERE name = '에테르넬 시프셔츠'")
				database.execSQL("UPDATE items SET itemSetCode = 403 WHERE name = '에테르넬 시프팬츠'")
				database.execSQL("UPDATE items SET itemSetCode = 503 WHERE name = '에테르넬 파이렛햇'")
				database.execSQL("UPDATE items SET itemSetCode = 503 WHERE name = '에테르넬 파이렛코트'")
				database.execSQL("UPDATE items SET itemSetCode = 503 WHERE name = '에테르넬 파이렛팬츠'")
			}
		}
		private val MIGRATION_15_16 = object : Migration(15, 16) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("UPDATE items SET canCube = 1 WHERE name = '에테르넬 나이트숄더'")
				database.execSQL("UPDATE items SET canCube = 1 WHERE name = '에테르넬 아처숄더'")
				database.execSQL("UPDATE items SET canCube = 1 WHERE name = '에테르넬 메이지숄더'")
				database.execSQL("UPDATE items SET canCube = 1 WHERE name = '에테르넬 시프숄더'")
				database.execSQL("UPDATE items SET canCube = 1 WHERE name = '에테르넬 파이렛숄더'")
			}
		}

		private val MIGRATION_16_17 = object : Migration(16, 17) {
			override fun migrate(database: SupportSQLiteDatabase) {
			}
		}

		private val MIGRATION_17_18 = object : Migration(17, 18) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("UPDATE items SET amazing = 0")
			}
		}

		private val MIGRATION_18_19 = object : Migration(18, 19) {
			override fun migrate(database: SupportSQLiteDatabase) {
			}
		}

		private val MIGRATION_19_20 = object : Migration(19, 20) {
			override fun migrate(database: SupportSQLiteDatabase) {
			}
		}

		private val MIGRATION_20_21 = object : Migration(20, 21) {
			override fun migrate(database: SupportSQLiteDatabase) {
			}
		}

		private val MIGRATION_21_22 = object : Migration(21, 22) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("DELETE from itemTable")
				database.execSQL("DELETE from cubeTable")
				database.execSQL("UPDATE items SET canCube = 0 WHERE name = '에테르넬 나이트숄더'")
				database.execSQL("UPDATE items SET canCube = 0 WHERE name = '에테르넬 아처숄더'")
				database.execSQL("UPDATE items SET canCube = 0 WHERE name = '에테르넬 메이지숄더'")
				database.execSQL("UPDATE items SET canCube = 0 WHERE name = '에테르넬 시프숄더'")
				database.execSQL("UPDATE items SET canCube = 0 WHERE name = '에테르넬 파이렛숄더'")
			}
		}

		private val MIGRATION_22_23 = object : Migration(22, 23) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("DELETE from itemTable")
				database.execSQL("ALTER TABLE itemTable ADD COLUMN canCreate INTEGER NOT NULL default 0")
			}
		}

		private val MIGRATION_23_24 = object : Migration(23, 24) {
			override fun migrate(database: SupportSQLiteDatabase) {

			}
		}

		private val MIGRATION_24_25 = object : Migration(24, 25) {
			override fun migrate(database: SupportSQLiteDatabase) {
			}
		}

		private val MIGRATION_25_26 = object : Migration(25, 26) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("DELETE from itemTable")
			}
		}

		private val MIGRATION_26_27 = object : Migration(26, 27) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("DELETE from cubeTable")
			}
		}

		private var instance: Database? = null

		@OptIn(InternalCoroutinesApi::class)
		@Synchronized
		fun getInstance(context: Context): Database? {
			if(instance == null) {
				synchronized(Database::class) {
					instance = Room.databaseBuilder(
						context.applicationContext,
						Database::class.java,
						"user.db").addMigrations(MIGRATION_1_2).addMigrations(MIGRATION_2_3)
						.addMigrations(MIGRATION_3_4).addMigrations(MIGRATION_4_5)
						.addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
						.addMigrations(MIGRATION_9_10)
						.addMigrations(MIGRATION_10_11)
						.addMigrations(MIGRATION_11_12)
						.addMigrations(MIGRATION_12_13)
						.addMigrations(MIGRATION_13_14)
						.addMigrations(MIGRATION_14_15)
						.addMigrations(MIGRATION_15_16)
						.addMigrations(MIGRATION_16_17)
						.addMigrations(MIGRATION_17_18)
						.addMigrations(MIGRATION_18_19)
						.addMigrations(MIGRATION_19_20)
						.addMigrations(MIGRATION_20_21)
						.addMigrations(MIGRATION_21_22)
						.addMigrations(MIGRATION_22_23)
						.addMigrations(MIGRATION_23_24)
						.addMigrations(MIGRATION_24_25)
						.addMigrations(MIGRATION_25_26)
						.addMigrations(MIGRATION_26_27)
						.build()
				}
			}
			return instance
		}
	}
}