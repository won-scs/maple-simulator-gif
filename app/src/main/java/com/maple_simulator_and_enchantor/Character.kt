package com.maple_simulator_and_enchantor

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.maple_simulator_and_enchantor.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Integer.min
import java.security.SecureRandom
import kotlin.math.floor

class Character {
	var characterId = 0 // 캐릭터 아이디
	var characterLvl = 300 // 캐릭터 레벨
	private val statKindNum = 36
	var totalStat = MutableList(100) { 0 }
	var totalArmIgn = 0.0
	var baseStat = MutableList(100) { 0 } // 기본 스탯
	private var baseArmIgn = 0.0
	var itemStat = MutableList(100) { 0 }
	private var itemArmIgn = 0.0
	var setStat = MutableList(100) { 0 }
	var setArmIgn = 0.0

	var statAtkFront = 0.0 // 앞스공
	var statAtkBack = 0.0 // 뒷스공
	var dopStatAtkFront = 0.0 // 도핑 앞스공
	var dopStatAtkBack = 0.0 // 도핑 뒷스공
	var proficiency = 90.0 // 숙련도
	var weaponConstant = 0.0 // 무기 상수

	var ringId = listOf<Int>(0, 0, 0, 0).toMutableList()
  var pendantId = listOf<Int>(0, 0).toMutableList()

	lateinit var db: Database
	private var character: Characters
	private lateinit var wearItems: List<Items>
	private var cubeTables: List<CubeTable>
	private var itemsDao: ItemsDao
	private var itemTableDao: ItemTableDao
	var jobName = ""
	lateinit var mainStat: List<Double>
	lateinit var subStat: List<Double>
	val setOptionList = MutableList(1000){ emptyList<Int>().toMutableList() }
	private val setOptionTable = AppData.setOptionMade()

	private fun setMainSubStat() {
		if(jobName in listOf("히어로", "팔라딘", "다크나이트", "소울마스터", "아델", "블래스터", "아란", "바이퍼", "캐논슈터", "카이저")) {
			mainStat = listOf(1.0, 0.0, 0.0, 0.0, 0.0)
			subStat = listOf(0.0, 1.0, 0.0, 0.0, 0.0)
		} else if(jobName in listOf("보우마스터", "신궁", "패스파인더", "윈드브레이커", "와일드헌터", "메르세데스", "캡틴", "메카닉", "엔젤릭버스터")) {
			mainStat = listOf(0.0, 1.0, 0.0, 0.0, 0.0)
			subStat = listOf(1.0, 0.0, 0.0, 0.0, 0.0)
		} else if(jobName in listOf("아크메이지(썬_콜)", "아크메이지(불_독)", "비숍", "플레임위자드", "루미너스", "에반", "배틀메이지")) {
			mainStat = listOf(0.0, 0.0, 1.0, 0.0, 0.0)
			subStat = listOf(0.0, 0.0, 0.0, 1.0, 0.0)
		} else if(jobName in listOf("나이트로드", "나이트워커", "팬텀")) {
			mainStat = listOf(0.0, 0.0, 0.0, 1.0, 0.0)
			subStat = listOf(0.0, 1.0, 0.0, 0.0, 0.0)
		} else if(jobName in listOf("듀얼블레이드", "섀도어", "카데나")) {
			mainStat = listOf(0.0, 0.0, 0.0, 1.0, 0.0)
			subStat = listOf(1.0, 1.0, 0.0, 0.0, 0.0)
		} else if(jobName in listOf("제논")) {
			mainStat = listOf(0.7, 0.7, 0.0, 0.7, 0.0)
			subStat = listOf(0.0, 0.0, 0.0, 0.0, 0.0)
		} else {
			mainStat = listOf(1.0, 0.0, 0.0, 0.0, 0.0)
			subStat = listOf(0.0, 1.0, 0.0, 0.0, 0.0)
		}
	}

	private fun setBaseStat() {
		baseStat = character.baseStat.toMutableList() // 기본 스탯 로드
		for(i in 0 .. 3)
			baseStat[i * 2] += (mainStat[i] * characterLvl * 5).toInt() // 레벨업 스탯포인트
		baseArmIgn = character.baseArmIgn
	}

	constructor(character: Characters, itemsDao: ItemsDao, itemTableDao: ItemTableDao, cubeTables: List<CubeTable>, characterId: Int) {
		this.characterId = characterId; this.character = character; this.itemsDao = itemsDao
		this.itemTableDao = itemTableDao; this.cubeTables = cubeTables; this.jobName = character.jobName

		setMainSubStat() // 주스탯과 부스탯 설정
		setBaseStat() // 기본 스탯 설정

		for(i in 1..4) {
			ringId[i-1] = character.getRingId(i)
			if(i <= 2)
				pendantId[i-1] = character.getPendantId(i)
		}
		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				wearItems = itemsDao.getByWearCharacterId(characterId)
			}.join()
		}

		for(wearItem in wearItems) {
			val itItem = Item(itemsDao, wearItem, cubeTables)
			if(wearItem.itemSetCode != 0) {
				for(setOption in setOptionTable) {
					if(setOptionList[setOption.key.first].size == setOption.value.first.size) continue
					if(setOption.key.first == wearItem.itemSetCode && !setOptionList[wearItem.itemSetCode].contains(wearItem.itemType%100)) {
						setOptionList[wearItem.itemSetCode].add(wearItem.itemType%100) // 세트 아이템
						break
					} else if(wearItem.itemSetCode == 777 && setOption.value.first.contains(wearItem.itemType%100)
						&& !setOptionList[setOption.key.first].contains(wearItem.itemType%100)) {
						setOptionList[setOption.key.first].add(wearItem.itemType%100) // 럭키 아이템
					}
				}
			}
			if(wearItem.itemType % 100 == 0) {
			  weaponConstant = AppData.itemJob[wearItem.name]?.second!!
			}
			for(i in 0 until 100) {
				if(i != 17) itemStat[i] += itItem.totalStat[i]
				else itemArmIgn += (100-itemArmIgn) * (itItem.totalArmIgn) / 100
			}
		}
		totalCalculate()
	}

	private fun totalCalculate() {
		setStat = MutableList<Int>(100) { 0 }
		setArmIgn = 0.0
		for(i in setOptionList.indices) {
			if(setOptionList[i].size != 0) {
				for(setOption in setOptionTable) {
					if(setOption.key.first == i) {
						for(j in 2 .. min(setOptionList[i].size, setOption.value.second.size-1)) {
							for (k in 0 until 100) {
								if(k == 17) {
									try {
										setArmIgn += (100 - setArmIgn) * (setOption.value.second[j][k]).toDouble() / 100
									} catch (_: Exception) {}
								}
								else {
									try {
										setStat[k] += setOption.value.second[j][k]
									} catch (_: Exception) {}
								}
							}
						}
					}
				}
			}
		}

		totalStat[13] = baseStat[13] + itemStat[13] // 올스탯%

		for(i in listOf(1, 3, 5, 7))
			totalStat[i] = baseStat[i] + itemStat[i] + totalStat[13] // 힘덱인럭%

		for(i in listOf(0, 2, 4, 6))
			totalStat[i] = (baseStat[i] + itemStat[i] + itemStat[12] + setStat[12] + itemStat[28 + i/2] * (characterLvl / 9 )) * (100 + totalStat[i+1]) / 100 // 힘덱인럭

		// 심볼 스탯
		if(jobName == "제논") {
			totalStat[0] += ((13200 + 15000) * 0.48).toInt()
			totalStat[2] += ((13200 + 15000) * 0.48).toInt()
			totalStat[6] += ((13200 + 15000) * 0.48).toInt()
		} else {
			for (i in 0..3) {
				totalStat[2 * i] += ((13200 + 15000) * mainStat[i]).toInt()
			}
		}
		for(i in listOf(9, 11, 14, 15, 21, 23, 25))
			totalStat[i] = baseStat[i] + itemStat[i] // 공%, 마%, 점프력, 이동속도, HP%, MP%, 방어력%
		totalStat[8] = (baseStat[8] + itemStat[8] + itemStat[32] * characterLvl / 9 + setStat[8]) // 공격력
		totalStat[10] = (baseStat[10] + itemStat[10] + itemStat[33] * characterLvl / 9 + setStat[10]) // 마력
		for(i in listOf(20, 22, 24))
			totalStat[i] = (baseStat[i] + itemStat[i]) * (100 + itemStat[i+1]) / 100 // HP, MP, 방어력
		totalStat[18] = baseStat[18] + itemStat[18] + 30 // 데미지(하이퍼 포함)
		totalStat[19] = baseStat[19] + itemStat[19] + setStat[19] + 70 + 70// 보스 데미지(유니온, 하이퍼, 2중 마약 포함)
		totalStat[34] = baseStat[34] + itemStat[34] + 50 // 크리티컬 데미지 (유니온, 하이퍼 포함)
		totalArmIgn = baseArmIgn + ((100-baseArmIgn) * itemArmIgn / 100)
		totalArmIgn += ((100 - totalArmIgn) * 40.0 / 100) // 유니온 스탯
		totalArmIgn += ((100 - totalArmIgn) * 40.0 / 100) // 마약 버프
		totalArmIgn += ((100 - totalArmIgn) * 30.0 / 100) // 하이퍼 스탯
		totalArmIgn += ((100 - totalArmIgn) * 30.0 / 100) // 마약 칭호
		totalArmIgn += ((100 - totalArmIgn) * setArmIgn / 100) // 세트 옵션
		totalStat[55] = baseStat[55] + itemStat[55] // 최종 데미지
	}

	fun statAtkCalculate() {
		totalCalculate()
		var mainStatValue = 0
		var subStatValue = 0
		for(i in 0 .. 3) {
			mainStatValue += ((totalStat[2*i]) * mainStat[i]).toInt()
			subStatValue += (totalStat[2*i] * subStat[i]).toInt()
		}
		val atkOrSpl = if(mainStat[2].toInt() != 0) totalStat[10] else totalStat[8]
		val atkOrSplPercent = if(mainStat[2].toInt() != 0) totalStat[11] else totalStat[9]
		statAtkBack = (mainStatValue * 4 + subStatValue) * 0.01 * atkOrSpl.toDouble()
		statAtkBack *= weaponConstant * (100 + atkOrSplPercent).toDouble() * 0.01 * (100 + totalStat[55]) * 0.01 * (100 + totalStat[18]) * 0.01
		statAtkFront = baseStat[56].toDouble() / 100.0 * statAtkBack
	}

	private val dopDmg = 30.0 + 30.0 + 20.0 + 10.0 + 12.0 + 12.0 + 9.0 + 9.0 + 15.0 + 10.0 // 길스(뎀퍼) + 길스(보뎀) + 반빨별 + 대영비 + 카데나 + 아크 + 모법 + 모도 + 데슬 + 데벤

	private fun dopStatAtkCalculate() {
		totalCalculate()
		var mainStatValue = 30
		var subStatValue = 30
		for(i in 0 .. 3) {
			mainStatValue += ((totalStat[2*i]) * mainStat[i]).toInt()
			subStatValue += (totalStat[2*i] * subStat[i]).toInt()
		}
		val atkOrSpl = if(mainStat[2].toInt() != 0) totalStat[10] + 175 else totalStat[8] + 175 // 4뿌리기, 시그링크, 익스레드or블루
		val atkOrSplPercent = if(mainStat[2].toInt() != 0) totalStat[11] else totalStat[9]
		dopStatAtkBack = (mainStatValue * 4 + subStatValue) * 0.01 * atkOrSpl.toDouble()
		dopStatAtkBack *= weaponConstant * (100 + atkOrSplPercent).toDouble() * 0.01 * (100 + totalStat[55]) * 0.01//
		dopStatAtkFront = baseStat[56].toDouble() / 100.0 * dopStatAtkBack
	}

	fun combatCalculate(): Int {
		var combatPowerStat = 0
		for(i in 0..3) combatPowerStat += (mainStat[i].toInt() * 4 + subStat[i].toInt()) * (itemStat[2*i] + setStat[2*i]) * (100 + itemStat[2*i + 1]) / 10000
		combatPowerStat += (132 + 150)
		val combatPowerAtkOrSpl = if(itemStat[8] > itemStat[10]) itemStat[8] * (100 + itemStat[9]) / 100 else itemStat[10] * (100 + itemStat[11]) / 100
		val combatPowerDmg = 100 + itemStat[19] + itemStat[18] + setStat[19] + setStat[18]
		val combatPowerCri = 135 + itemStat[34] + setStat[34]
		Log.d("message", "${combatPowerStat} ${combatPowerAtkOrSpl} ${combatPowerDmg} ${combatPowerCri}")
		val combatPower = combatPowerStat * combatPowerAtkOrSpl * (combatPowerDmg.toDouble() / 100.0) * (combatPowerCri.toDouble() / 100.0) * 1.1
		return combatPower.toInt()
	}

	fun skillDamage(skillDamage: List<Int>, bossarm: Double): List<Long> {
		dopStatAtkCalculate()
		val secureRandom = SecureRandom()
		val damageList = emptyList<Long>().toMutableList()
		val armIgnCorr = 0.0.coerceAtLeast((100 - (bossarm - 3 * totalArmIgn))) / 100 // 방어율 무시를 적용했을 때 들어가는 데미지 비율 (ex: 방어율 300%, 방무 90%, 변수값 0.7)
		val criDmgCorr = (135.0 + 30.0 + 4.0 + totalStat[34]) / 100.0 // 크리티컬 데미지 보정 (길스 + 키네 링크)
		val dmgPerCorr = (100.0 + dopDmg + totalStat[18] + totalStat[19]) / 100.0 // 데미지% 보정

		for(i in skillDamage.indices) {
			val skillDmgCorr = skillDamage[i].toDouble()/100.0 // 스킬 데미지 보정
			val randomStatAtk = dopStatAtkFront.toLong() + secureRandom.nextInt(dopStatAtkBack.toInt() - dopStatAtkFront.toInt() + 1)
			val skillDmg = armIgnCorr * randomStatAtk * skillDmgCorr * criDmgCorr * dmgPerCorr * 2.2
			damageList.add(skillDmg.toLong()*2)
		}
		return damageList
	}
}