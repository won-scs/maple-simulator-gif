package com.maple_simulator_and_enchantor

import android.annotation.SuppressLint
import com.maple_simulator_and_enchantor.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.security.SecureRandom
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.max
import kotlin.random.Random.Default.nextInt

open class Item {

	var totalStat = MutableList<Int>(100) { 0 }
	var totalArmIgn = 0.0
	var baseStat = MutableList<Int>(100) { 0 }
	var baseArmIgn = 0.0
	var extraStat = MutableList<Int>(100) { 0 }
	var extraArmIgn = 0.0
	var starStat = MutableList<Int>(100) { 0 }
	var starArmIgn = 0.0
	var enchantStat = MutableList<Int>(100) { 0 }
	var enchantArmIgn = 0.0
	var cubeStat = MutableList<Int>(100) { 0 }
	var cubeArmIgn = 0.0

	var name: String // 아이템 이름
	var wearCharacterId = 0 // 착용한 캐릭터의 아이디

	var upAbility: MutableList<Int>
	var downAbility: MutableList<Int>

	var setCode = 0
	var itemLvl = 0
	var itemCode = 0
	var itemId = 0
	var itemType = 0
	var itemTypeNameList = listOf("무기", "보조무기", "엠블렘", "모자", "상의", "하의", "상하의",
		"신발", "장갑", "망토", "어깨장식", "눈장식", "얼굴장식", "귀고리", "포켓", "벨트", "훈장",
		"반지", "펜던트", "기계심장", "벳지", "방패")

	var canCube = true
	var canFlame = true

	var canEnchant = true
	var remainUpGrade = 0
	var upGrade = 0
	var goldHammer = 0
	var upGradeRestore = 0
	var amazing = 0

	var canStarForce = false
	var currentStar = 0
	var maxStar = 0


	lateinit var db: Database
	var itemsDao: ItemsDao
	lateinit var itemTableDao: ItemTableDao
	//lateinit var itemTables: List<ItemTable>
	var itemData: Items
	//lateinit var itemTableData: ItemTable
	lateinit var cubeTableDao: CubeTableDao
	var cubeTables: List<CubeTable>

	var useMeso: Long = 0L
	var useCash: MutableList<Int>
	var destroyNum = 0

	var mainStat = mutableListOf(0, 0, 0, 0, 0)
	var subStat = mutableListOf(0, 0, 0, 0, 0)

	constructor(itemsDao: ItemsDao, itemData: Items, cubeTables: List<CubeTable>) {
		this.itemsDao = itemsDao
		this.itemData = itemData
		//this.itemTableData = itemTableData
		this.cubeTables = cubeTables

		baseStat = itemData.baseStat.toMutableList()
		baseArmIgn = itemData.baseArmIgn
		extraStat = itemData.extraStat.toMutableList()
		extraArmIgn = itemData.extraArmIgn
		starStat = itemData.starStat.toMutableList()
		starArmIgn = itemData.starArmIgn
		enchantStat = itemData.enchantStat.toMutableList()
		enchantArmIgn = itemData.enchantArmIgn

		maxStar = itemData.maxStar
		upGrade = itemData.upGrade
		remainUpGrade = itemData.remainUpGrade
		upGradeRestore = itemData.upGradeRestore
		amazing = itemData.amazing
		if(remainUpGrade == 0 && upGradeRestore == 0) {
			var update = false
			for(i in enchantStat) if(i != 0) update = true
			if(!update)
				remainUpGrade = upGrade
		}
		goldHammer = itemData.goldHammer
		itemType = itemData.itemType
		when(itemType/100) {
			1, 2, 5 -> {mainStat[0] = 1; subStat[1] = 1}
			3 -> {mainStat[2] = 1; subStat[3] = 1}
			4 -> {mainStat[3] = 1; subStat[1] = 1}
			6 -> {mainStat = mutableListOf(1, 1, 1, 1, 0)}
		}
		itemCode = itemData.itemCode
		itemId = itemData.id
		wearCharacterId = itemData.wearCharacterId
		itemLvl = itemData.itemLvl
		name = itemData.name
		setCode = itemData.itemSetCode

		canStarForce = when(itemData.canStarForce){0->true;1->false;else->true}
		currentStar = itemData.currentStar
		canFlame = when(itemData.canFlame){0->true;1->false;else->true}
		canEnchant = when(itemData.canEnchant){0->true;1->false;else->true}
		canCube = when(itemData.canCube){0->true;1->false;else->true}

		if(name.contains("제네시스")) {
			currentStar = 22
			remainUpGrade = 0
			if(name.contains("에너지체인")) {enchantStat[2] = 24; enchantStat[6] = 24}
			else enchantStat[mainStat[1]*2 + mainStat[2]*4 + mainStat[3]*6] = 32
			if (name.contains("스태프") || name.contains("완드")) {enchantStat[10] = 72}
			else {enchantStat[8] = 72}
		} else if(name.contains("블랙 하트")) {
			currentStar = 15
		}

		starForceUpStat = when(itemLvl) {
			250 -> listOf<Int>(2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 17, 17, 17, 17, 17, 17, 17, 0, 0, 0)
			200 -> listOf<Int>(2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 15, 15, 15, 15, 15, 15, 15, 0, 0, 0)
			160 -> listOf<Int>(2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 13, 13, 13, 13, 13, 13, 13, 0, 0, 0)
			150 -> listOf<Int>(2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 11, 11, 11, 11, 11, 11, 11, 0, 0, 0)
			140 -> listOf<Int>(2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 9, 9, 9, 9, 9, 9, 9, 0, 0, 0)
			130, 135 -> listOf<Int>(2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 7, 7, 7, 7, 7)
			else -> listOf<Int>(2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3)
		} // 스타포스 스탯 오르는 수치

		starForceUpHp = when(itemType % 100) {
			0, 1, 3, 4, 5, 6, 9, 10, 15, 17, 18, 21 ->
				listOf(5, 5, 5, 10, 10, 15, 15, 20, 20, 25, 25, 25, 25, 25, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
			else -> listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
		} // 스타포스 Hp 오르는 수치


		upAbility = itemData.upAbilityList.toMutableList()
		downAbility = itemData.downAbilityList.toMutableList()


		if(canCube && upAbility[0] == 0) { upAbility[0] = 1
			runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				itemsDao.updateDownAbility(downAbility, itemId)
			}.join()
		}}
		if(canCube && downAbility[0] == 0) { downAbility[0] = 1
			runBlocking {
				CoroutineScope(Dispatchers.IO).launch {
					itemsDao.updateDownAbility(downAbility, itemId)
				}.join()
			}}

		useMeso = itemData.useMesoInfo
		useCash = itemData.useCashInfoList.toMutableList()
		destroyNum = itemData.destroyNum

		cubeStatCalculate()
		totalCalculate()
	}

	private fun totalCalculate() {
		starForceUpAtk = when(itemType % 100) {
			0, 1 -> {
				val to15UpAtk = emptyList<Int>().toMutableList()
				var j = 0
				for(i in 0 until 15) {
					val k = (baseStat[8]+enchantStat[8]+j) / 50 + 1
					to15UpAtk.add(k)
					j += k
				}
				when(itemLvl) {
					200 -> to15UpAtk.addAll(listOf(13, 13, 14, 14, 15, 16, 17, 34, 35, 36))
					160 -> to15UpAtk.addAll(listOf(9, 9, 10, 11, 12, 13, 14, 32, 33, 34))
					150 -> to15UpAtk.addAll(listOf(8, 9, 9, 10, 11, 12, 13, 31, 32, 33))
					140 -> to15UpAtk.addAll(listOf(7, 8, 8, 9, 10, 11, 12, 30, 31, 32))
					130 -> to15UpAtk.addAll(listOf(6, 7, 7, 8, 9))
				}
				to15UpAtk
			}
			else -> {
				when(itemLvl) {
					250 -> listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 15, 16, 17, 18, 19, 21, 23, 25, 27)
					200 -> listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 13, 14, 15, 16, 17, 19, 21, 23, 25)
					160 -> listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 11, 12, 13, 14, 15, 17, 19, 21, 23)
					150 -> listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 10, 11, 12, 13, 14, 16, 18, 20, 22)
					140 -> listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 10, 11, 12, 13, 15, 17, 19, 21)
					130, 135 -> listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 8, 9, 10, 11)
					else -> listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
				}
			}
		} // 스타포스 공격력 오르는 수치
		starForceUpSpl = when(itemType % 100) {
			0, 1 -> {
				val to15UpSpl = emptyList<Int>().toMutableList()
				var j = 0
				for(i in 0 until 15) {
					val k = (baseStat[10]+enchantStat[10]+j) / 50 + 1
					to15UpSpl.add(k)
					j += k
				}
				when(itemLvl) {
					200 -> to15UpSpl.addAll(listOf(13, 13, 14, 14, 15, 16, 17, 34, 35, 36))
					160 -> to15UpSpl.addAll(listOf(9, 9, 10, 11, 12, 13, 14, 32, 33, 34))
					150 -> to15UpSpl.addAll(listOf(8, 9, 9, 10, 11, 12, 13, 31, 32, 33))
					140 -> to15UpSpl.addAll(listOf(7, 8, 8, 9, 10, 11, 12, 30, 31, 32))
					130 -> to15UpSpl.addAll(listOf(6, 7, 7, 8, 9))
				}
				to15UpSpl
			}
			else -> {
				when(itemLvl) {
					250 -> listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 15, 16, 17, 18, 19, 21, 23, 25, 27)
					200 -> listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 13, 14, 15, 16, 17, 19, 21, 23, 25)
					160 -> listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 11, 12, 13, 14, 15, 17, 19, 21, 23)
					150 -> listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 10, 11, 12, 13, 14, 16, 18, 20, 22)
					140 -> listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 10, 11, 12, 13, 15, 17, 19, 21)
					130, 135 -> listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 8, 9, 10, 11)
					else -> listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
				}
			}
		} // 스타포스 마력 오르는 수치
		if(amazing == 0) {
			starStat = MutableList<Int>(100) { 0 }
			if (currentStar != 0) {
				for (i in 0 until currentStar) {
					if (baseStat[8] != 0)
						starStat[8] += starForceUpAtk[i];
					if (baseStat[10] != 0)
						starStat[10] += starForceUpSpl[i];
					starStat[20] += starForceUpHp[i]
					val upStat = starForceUpStat[i]
					for (j in mainStat.indices) {
						starStat[j * 2] += upStat * (mainStat[j] + subStat[j])
					}
				}
			}
		}
		for(i in 0 until 100)
			totalStat[i] = baseStat[i] + extraStat[i] + starStat[i] + enchantStat[i] + cubeStat[i]
		totalArmIgn = (1.0 - (1.0 - baseArmIgn/100) * (1.0 - extraArmIgn/100) * (1.0 - starArmIgn/100) * (1.0 - enchantArmIgn/100) * (1.0 - cubeArmIgn/100)) * 100
	}

	fun extraOptionChange(flameType: Int) {
		val beforeExtraStat = extraStat // 사용 이전 추가 옵션 저장
		extraStat = MutableList<Int>(100) { 0 }

		val optionSet = emptySet<Int>().toMutableSet<Int>()
		val optionGradeTable = when(flameType) {
			1, 3 -> listOf(0, 29, 74, 99, 100) // 영환불, 검환불
			2 -> listOf(20, 50, 86, 100, 100) // 강환불
			else -> listOf(0, 29, 74, 99, 100)
		}

		while(optionSet.size < 4) {
			optionSet.add(SecureRandom().nextInt(19)+1)
		}

		for(i in optionSet) {
			val optionGrade: Int = SecureRandom().nextInt(100) + 1
			if (optionGrade <= optionGradeTable[0]) {
				addExtraOption(i, 3)
				continue
			}
			for (j in 1..4) {
				if (optionGradeTable[j] >= optionGrade) {
					addExtraOption(i, j + 3)
					break
				}
			}
		}

	}

	private fun addExtraOption(optionType: Int, optionGrade: Int) {
		var itemLvlHpMp: Int = itemLvl * 3
		var itemLvlSingleStat: Int = itemLvl
		if(itemLvl == 250) {
			itemLvlHpMp = 700
			itemLvlSingleStat = 220
		}

		when(optionType) {
			1 -> extraStat[0] += ((itemLvlSingleStat/20) + 1) * optionGrade
			2 -> extraStat[2] += ((itemLvlSingleStat/20) + 1) * optionGrade
			3 -> extraStat[4] += ((itemLvlSingleStat/20) + 1) * optionGrade
			4 -> extraStat[6] += ((itemLvlSingleStat/20) + 1) * optionGrade
			5, 6, 7, 8, 9, 10 -> { 	// 이중 추옵
				var statA = 0; var statB = 0
				when(optionType) {
					5 -> {statA = 0; statB = 2} // str, dex
					6 -> {statA = 0; statB = 4} // str, int
					7 -> {statA = 0; statB = 6} // str, luk
					8 -> {statA = 2; statB = 4} // dex, int
					9 -> {statA = 2; statB = 6} // dex, luk
					10 -> {statA = 4; statB = 6} // int, luk
				}
				extraStat[statA] += ((itemLvl/40) + 1) * optionGrade
				extraStat[statB] += ((itemLvl/40) + 1) * optionGrade
			}
			11 -> extraStat[20] += itemLvlHpMp * optionGrade // HP
			12 -> extraStat[22] += itemLvlHpMp * optionGrade // MP
			13 -> extraStat[16] += 5 * optionGrade // 착용 제한 레벨 감소
			14 -> extraStat[24] += itemLvlSingleStat * optionGrade // 방어력
			15, 16 -> { // 공격력, 마력
				val atkOrSpl = when(optionType) {15 -> 8; 16 -> 10; else -> 8}
				extraStat[atkOrSpl] += when(itemType % 100) {
					0 -> ceil(Math.max(baseStat[8], baseStat[10]).toDouble() / 100 * (itemLvl/40 + 1) * optionGrade * Math.pow(1.1, (optionGrade-3).toDouble())).toInt()
					else -> optionGrade
				}
			}
			17 -> { when(itemType % 100) {	0 -> extraStat[19] += 2 * optionGrade;	else -> extraStat[15] += optionGrade } } // 무기면 보뎀, 그 외는 이동속도
			18 -> { when(itemType % 100) {0 -> extraStat[18] += optionGrade;	else -> extraStat[14] += optionGrade } } // 무기면 데미지, 그 외는 점프력
			19 -> { extraStat[13] += optionGrade } // 올스탯%
		}
	}

	private val starForceSuccessProb = listOf<Int>(9500, 9000, 8500, 8000, 7500, 7000, 6500, 6000, 5500, 5000, 5000, 4500, 4000, 3500, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 300, 200, 100)
	private val starForceDestroyProb = listOf<Int>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 210, 210, 210, 280, 280, 700, 700, 1940, 2940, 3960)
	lateinit var starForceUpStat: List<Int>
	private lateinit var starForceUpAtk: List<Int>
	private lateinit var starForceUpSpl: List<Int>
	lateinit var starForceUpHp: List<Int>
	lateinit var starForceUpMp: List<Int>
	private val starForceDownLvl = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1)

	fun nextStarForceText(starCatch: Boolean, notDestroy: Boolean, success100: Boolean, chanceTime: Boolean): String {
		val successProb: Double =
			if(chanceTime || (success100 && (currentStar==5 || currentStar==10 || currentStar==15))) {10000.0}
			else if(starCatch) {starForceSuccessProb[currentStar]*1.05}
			else {starForceSuccessProb[currentStar].toDouble()} // 성공 확률
		val destroyProb: Double =
			if(chanceTime || (success100 && (currentStar==5 || currentStar==10 || currentStar==15))) {0.0}
			else if(notDestroy && (currentStar in 12..16)) {0.0}
			else if(starCatch) {
				starForceDestroyProb[currentStar].toDouble() -
					(successProb-starForceSuccessProb[currentStar]) * starForceDestroyProb[currentStar].toDouble() /
					(10000.0-starForceSuccessProb[currentStar])
			}
			else {starForceDestroyProb[currentStar].toDouble()} // 파괴 확률
		var string: String = ""
		string += "${currentStar}성 > ${currentStar+1}성\n\n"
		if(chanceTime) string += "찬스타임!\n"
		string += "성공확률: ${String.format("%.1f", successProb/100)}%\n"
		string += if(currentStar >= 16 && currentStar != 20) "실패(하락)확률" else "실패(유지)확률"
		string += ": ${String.format("%.1f", 
			(10000 - successProb - destroyProb)/100)}%\n"
		string += "파괴확률: ${String.format("%.1f", destroyProb/100)}%\n\n"
		val upStat = starForceUpStat[currentStar]
		string += when(itemType / 100) {
			1 -> { "STR: +${upStat}\n" + "DEX: +${upStat}\n" }
			else -> { "STR: +${upStat}\n" + "DEX: +${upStat}\n" + "INT: +${upStat}\n" + "LUK: +${upStat}\n" }
		}
		string += "공격력: +${starForceUpAtk[currentStar]}\n마력: +${starForceUpAtk[currentStar]}\n"
		string += "최대 Hp: +${starForceUpHp[currentStar]}\n"
		return string
	} // 스타포스 강화 텍스트

	fun nextStarForceCost(starCatch: Boolean, notDestroy: Boolean, costSale30: Boolean, success100: Boolean, chanceTime: Boolean): String {
		var meso = 0L
		var mesoWeight = listOf(400, 220, 150, 110, 75)
		val deciFormat =  DecimalFormat("#,###")
		meso = if(currentStar <= 9) 1000 + (itemLvl*itemLvl*itemLvl*(currentStar+1)/25).toLong()
		else if(currentStar <= 14) 1000 + (itemLvl*itemLvl*itemLvl*Math.pow((currentStar+1).toDouble(),2.7)/mesoWeight[currentStar-10]).toLong()
		else 1000 + (itemLvl*itemLvl*itemLvl*Math.pow((currentStar+1).toDouble(),2.7)/200).toLong()
		if(notDestroy && (currentStar in 15..16) && !(success100 && currentStar == 15) && !chanceTime) meso *= 2
		if(costSale30) meso = (meso *0.7).toLong()
		return "강화 비용: ${deciFormat.format(meso)}메소"
	}

	fun starForceEnforce(starCatch: Boolean, notDestroy: Boolean, costSale30: Boolean, success100: Boolean, chanceTime: Boolean): Int { // 스타포스 강화
		if(maxStar == currentStar) return 0
		var meso = 0L
		var mesoWeight = listOf(400, 220, 150, 110, 75)
		meso = if(currentStar <= 9) 1000 + (itemLvl*itemLvl*itemLvl*(currentStar+1)/25).toLong()
		else if(currentStar <= 14) 1000 + (itemLvl*itemLvl*itemLvl*Math.pow((currentStar+1).toDouble(),2.7)/mesoWeight[currentStar-10]).toLong()
		else 1000 + (itemLvl*itemLvl*itemLvl*Math.pow((currentStar+1).toDouble(),2.7)/200).toLong()
		if(notDestroy && (currentStar in 15..16) && !(success100 && currentStar == 15) && !chanceTime) meso *= 2
		if(costSale30) meso = (meso *0.7).toLong()
		useMeso += meso
		var sucFailDest = 0
		val starForceRandomValue = SecureRandom().nextInt(10000) + 1
		val successProb: Double =
			if(chanceTime || (success100 && (currentStar==5 || currentStar==10 || currentStar==15))) {10000.0}
			else if(starCatch) {starForceSuccessProb[currentStar]*1.05}
			else {starForceSuccessProb[currentStar].toDouble()} // 성공 확률
		val destroyProb: Double =
			if(chanceTime || (success100 && (currentStar==5 || currentStar==10 || currentStar==15))) {0.0}
			else if(notDestroy && (currentStar in 15..16)) {0.0}
			else if(starCatch) {
				starForceDestroyProb[currentStar].toDouble() -
					(successProb-starForceSuccessProb[currentStar]) * starForceDestroyProb[currentStar].toDouble() /
					(10000.0-starForceSuccessProb[currentStar])
			}
			else {starForceDestroyProb[currentStar].toDouble()} // 파괴 확률

		if(starForceRandomValue <= successProb) { // 성공 시
			sucFailDest = 1
			starStat[20] += starForceUpHp[currentStar]
			if(baseStat[8] != 0) starStat[8] += starForceUpAtk[currentStar]
			if(baseStat[10] != 0) starStat[10] += starForceUpAtk[currentStar]
			val upStat = starForceUpStat[currentStar]
			when(itemType / 100) {
				1 -> {
					starStat[0] += upStat; 	starStat[2] += upStat
				}
				else -> {
					starStat[0] += upStat;  starStat[2] += upStat;	starStat[4] += upStat;	starStat[6] += upStat
				}
			}
			currentStar++
		} else if(starForceRandomValue > 10000-destroyProb){ // 파괴 시
			sucFailDest = 3
			destroyNum++
			currentStar = 12
			starStat[0] = 0; starStat[2] = 0;	starStat[4] = 0;	starStat[6] = 0
			starStat[8] = 0;	starStat[10] = 0; starStat[20] = 0
			for(i in 0..11) {
				if(baseStat[8] != 0) starStat[8] += starForceUpAtk[i];
				if(baseStat[10] != 0) starStat[10] += starForceUpAtk[i];
				starStat[20] += starForceUpHp[i]
				val upStat = starForceUpStat[i]
				when (itemType / 100) {
					1 -> { starStat[0] += upStat; starStat[2] += upStat }
					else -> { starStat[0] += upStat; starStat[2] += upStat;  starStat[4] += upStat; starStat[6] += upStat }
				}
			}
		} else { // 실패 시, 파괴 X
			sucFailDest = 2
			if(starForceDownLvl[currentStar] == 1) {
				currentStar--
				starStat[20] -= starForceUpHp[currentStar]
				starStat[8] -= starForceUpAtk[currentStar]
				starStat[10] -= starForceUpAtk[currentStar]
				val downStat = starForceUpStat[currentStar]
				when (itemType / 100) {
					1 -> { starStat[0] -= downStat;	starStat[2] -= downStat }
					else -> {	starStat[0] -= downStat;	starStat[2] -= downStat; starStat[4] -= downStat;	starStat[6] -= downStat }
				}
			} // 레벨 다운
		}
		totalCalculate() // 스탯 갱신

		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				itemsDao.updateStarStat(maxStar, currentStar, starStat, starArmIgn, itemId) // 스타포스 데이터 업데이트
				itemsDao.updateUseInfo(useMeso, useCash, destroyNum, itemId)
			}.join()
		}
		return sucFailDest
	}

	fun gradeUp(cubeType: Int, miracleTime: Boolean) {
		upAbility = MutableList<Int>(6){i -> upAbility[i]}
		downAbility = MutableList<Int>(6){i -> downAbility[i]}
		var gradeUpProb = 0
		when(cubeType) {
			1 -> gradeUpProb = when(upAbility[0]) {1 -> 60000; 2 -> 18000; 3 -> 3000; else -> 0} // 레드큐브 등업 확률
			2 -> gradeUpProb = when(upAbility[0]) {1 -> 150000; 2 -> 35000; 3 -> 14000; else -> 0} // 블랙큐브 등업 확률
			3, 4 -> gradeUpProb = when(downAbility[0]) {1 -> 47619; 2 -> 19608; 3 -> 7000; else -> 0} // 에디셔널 큐브 등업 확률
		}
		if(miracleTime) gradeUpProb *= 2
		val gradeUpRandomValue = secureRandom.nextInt(1000000) + 1
		if(gradeUpRandomValue <= gradeUpProb) {
			when(cubeType) {
				1, 2 -> upAbility[0]++
				3, 4 -> downAbility[0]++
			}
		}
	}

	lateinit var abilityOptionNameList: List<Int>

	var ableOptionTypeList: MutableList<Int> = emptyList<Int>().toMutableList()
	var ableOptionValueList: MutableList<Int> = emptyList<Int>().toMutableList()
	var ableOptionOneProbList: MutableList<Int> = emptyList<Int>().toMutableList()
	var ableOptionTwoProbList: MutableList<Int> = emptyList<Int>().toMutableList()
	var ableOptionThreeProbList: MutableList<Int> = emptyList<Int>().toMutableList()
	private val secureRandom = SecureRandom()

	private fun option1ChangeHelper(): Int {
		val randomOption = secureRandom.nextInt(1000000) + 1
		for (i in 0 until ableOptionOneProbList.size) {
			if (randomOption <= ableOptionOneProbList[i] || i == ableOptionOneProbList.size - 1) {
				return ableOptionValueList[i] * 100 + ableOptionTypeList[i]
			}
		}
		return 0
	} // 윗잠 첫번째 줄

	private fun option2ChangeHelper(option1: Int): Int {
		val randomOption = secureRandom.nextInt(1000000) + 1
		for (i in 0 until ableOptionTwoProbList.size) {
			if (randomOption <= ableOptionTwoProbList[i] || i == ableOptionTwoProbList.size - 1) {
				if(ableOptionTypeList[i] == option1%100) {
					when(ableOptionTypeList[i]) {
						37, 38, 39, 40, 41, 42, 43, 50 -> return option2ChangeHelper(option1)
					}
				}
				return ableOptionValueList[i] * 100 + ableOptionTypeList[i]
			}
		}
		return 0
	} // 윗잠 두번째 줄

	private fun option3ChangeHelper(option1: Int, option2: Int): Int {
		val randomOption = secureRandom.nextInt(1000000) + 1
		for (i in 0 until ableOptionThreeProbList.size) {
			if (randomOption <= ableOptionThreeProbList[i] || i == ableOptionThreeProbList.size - 1) {
				if(ableOptionTypeList[i] == option1%100 && ableOptionTypeList[i] == option2%100) {
					when(ableOptionTypeList[i]) {
						17, 19, 27, 44, 45, 46, 47, 53, 54 ->
						return option3ChangeHelper(option1, option2)
					}
				} // 3줄이 뜰 수 없는 옵션들
				else if((ableOptionTypeList[i] == option1%100 ||  ableOptionTypeList[i] == option2%100)) {
					when(ableOptionTypeList[i]) {
						37, 38, 39, 40, 41, 42, 43, 50 -> return option3ChangeHelper(option1, option2)
					}
				}
				return ableOptionValueList[i] * 100 + ableOptionTypeList[i]
			}
		}
		return 0
	} // 윗잠 두번째 줄

	@SuppressLint("Recycle")
	fun getOptionList(tableName: String) {
		ableOptionTypeList = emptyList<Int>().toMutableList()
		ableOptionValueList = emptyList<Int>().toMutableList()
		ableOptionOneProbList = emptyList<Int>().toMutableList()
		ableOptionTwoProbList = emptyList<Int>().toMutableList()
		ableOptionThreeProbList = emptyList<Int>().toMutableList()
		for(cubeTable in cubeTables) {
			if(cubeTable.tableName == "${tableName}Type") {
				for(i in 0 until 100)
					ableOptionTypeList.add(cubeTable.optionList[i])
			} else if(cubeTable.tableName == "${tableName}Value") {
				for(i in 0 until 100)
					ableOptionValueList.add(cubeTable.optionList[i])
			} else if(cubeTable.tableName == "${tableName}OneProb") {
				for(i in 0 until 100)
					ableOptionOneProbList.add(cubeTable.optionList[i])
			} else if(cubeTable.tableName == "${tableName}TwoProb") {
				for(i in 0 until 100)
					ableOptionTwoProbList.add(cubeTable.optionList[i])
			} else if(cubeTable.tableName == "${tableName}ThreeProb") {
				for(i in 0 until 100)
					ableOptionThreeProbList.add(cubeTable.optionList[i])
				break
			}
		}
	}

	fun optionChange(cubeType: Int) {
		val cubeName = when(cubeType) {
			1 -> "RedCube"
			2 -> "BlackCube"
			3, 4 -> "AdditionalCube"
			else -> "RedCube"
		}

		val itemGrade = when(when(cubeType) {1, 2 -> upAbility[0]; 3, 4 -> downAbility[0]; else -> 1
		}){ 1 -> "Rare"; 2 -> "Epic"; 3 -> "Unique"; 4 -> "Legendary"; else -> "Normal"}

		val itemOptionType = when(itemType % 100) {
			0 -> "Weapon" // 무기
			1, 21 -> "SubWeapon" // 보조무기, 방패
			2 -> "Emblem" // 엠블렘
			3 -> "Hat" // 모자
			4 -> "Armor" // 상의
			5 -> "Pants" // 하의
			7 -> "Shoes" // 신발
			8 -> "Gloves" // 장갑
			19 -> "Heart"
			11, 12, 13, 17, 18 -> "Acc" // 눈장식, 얼굴장식, 귀고리, 반지, 팬던트
			9, 10, 15 -> "Acc2" // 벨트, 견장, 망토
			else -> "Acc"
		}

		var optionLvl = ""
		if(itemLvl >= 250) {optionLvl = "250"}
		else if(itemLvl < 120) {optionLvl = "120"}
		getOptionList("${cubeName}${itemGrade}${itemOptionType}${optionLvl}")


		for (i in 1 until ableOptionThreeProbList.size) {
			ableOptionOneProbList[i] += ableOptionOneProbList[i - 1]
			ableOptionTwoProbList[i] += ableOptionTwoProbList[i - 1]
			ableOptionThreeProbList[i] += ableOptionThreeProbList[i - 1]
		}

		val option1 = option1ChangeHelper()
		val option2 = option2ChangeHelper(option1)
		val option3 = option3ChangeHelper(option1, option2)

		when(cubeType) {
			1, 2 -> { upAbility[2] = option1; upAbility[3] = option2; upAbility[4] = option3 }
			3, 4 -> { downAbility[2] = option1; downAbility[3] = option2; downAbility[4] = option3 }
		}

		cubeStatCalculate()
		totalCalculate()
	}

	private fun cubeStatCalculate() {
		cubeStat = MutableList<Int>(100) { 0 }
		for(i in 0 until 6) {
			val code = if(i<3){upAbility[i+2]} else{downAbility[i-1]}
			val value = code / 100
			when(code % 100) {
				17 -> cubeArmIgn += (100.0 - cubeArmIgn) * value.toDouble() / 100.0
				else -> cubeStat[code % 100] += value
			}
		}
	}

	fun enchant(enchantStat: MutableList<Int>, successProb: Int): Boolean {
		this.enchantStat = MutableList<Int>(100){i -> this.enchantStat[i]}
		if(remainUpGrade == 0) return false
		remainUpGrade--
		val random =  secureRandom.nextInt(100) + 1
		return if(successProb >= random) {
			for(i in 0 until 100) this.enchantStat[i] += enchantStat[i]
			true
		} else {
			upGradeRestore++
			false
		}
	}


	fun innocent(innocentType: Int, successProb: Int): Boolean {
		val random = secureRandom.nextInt(100) + 1
		return if(successProb >= random) {
			enchantStat = MutableList<Int>(100){0}
			goldHammer = 0
			upGradeRestore = 0
			remainUpGrade = upGrade
			if(amazing == 1) {
				amazing = 0
				canStarForce = true
				maxStar = if(itemLvl >= 138) 25
				else if(itemLvl >= 128) 20
				else if(itemLvl >= 118) 15
				else if(itemLvl >= 108) 10
				else if(itemLvl >= 95) 8
				else 5
				runBlocking {
					CoroutineScope(Dispatchers.IO).launch {
						itemsDao.updateCanStarForce(0, itemId)
					}.join()
				}
			}

			if(innocentType == 0) {
				currentStar = 0
				starStat = MutableList<Int>(100){0}
				runBlocking {
					CoroutineScope(Dispatchers.IO).launch {
						itemsDao.updateStarStat(maxStar, 0, MutableList<Int>(100){0}, 0.0, itemId) // 스타포스 데이터 업데이트
					}.join()
				}
			} // 일반 이노센트
			true
		} else {
			false
		}
	} // 이노센트

	fun goldHammer(successProb: Int): Boolean {
		if(goldHammer == 1) return false // 이미 황금망치가 적용된 상태라면
		val random = secureRandom.nextInt(100) + 1
		goldHammer = 1 // 성공 여부와 관계없이 제련 적용
		return if(successProb >= random) { // 성공했다면
			remainUpGrade++
			true
		} else { // 실패했다면
			upGradeRestore++
			false
		}
	} // 황금망치

	fun whiteEnchant(successProb: Int): Boolean {
		if(upGradeRestore == 0) return false // 복구 가능한 업그레이드 횟수가 없다면
		val random = secureRandom.nextInt(100) + 1
		return if(successProb >= random) { // 성공했다면
			remainUpGrade++ // 업횟 +1
			upGradeRestore-- // 복구 가능 업횟 -1
			true
		} else false // 실패했다면
	} // 순백의 주문서

	var amazingSuccessProb = listOf(60, 55, 50, 40, 30, 20, 19, 18, 17, 16, 14, 12, 10, 10, 10) // 놀장강 성공확률
	fun amazingEnchant(protected: Boolean): Int {
		if(currentStar >= 15 || currentStar >= maxStar) return 0 // 실패
		val random = secureRandom.nextInt(100) + 1
		return if(amazingSuccessProb[currentStar] >= random) {
			if(amazing == 0) {
				amazing = 1
				canStarForce = false
				maxStar = if (15 >= maxStar) maxStar else 15
			}
			val amazingStarStat = if(currentStar < 5) {
				when (itemLvl) {
					140 -> listOf(17, 18, 20, 23, 27)[currentStar]
					135 -> listOf(15, 16, 18, 21, 25)[currentStar]
					100 -> listOf(7, 8, 10, 13, 17)[currentStar]
					else -> listOf(7, 8, 10, 13, 17)[currentStar]
				}
			} else 0
			val amazingStarAtkSpl = if(currentStar >= 5) {
				max(itemLvl/10 - 6, 0) + listOf(1, 2, 3, 4, 5, 6, 8, 10, 12, 14)[currentStar-5]
			} else 0
			currentStar++
			for(i in listOf(0, 2, 4, 6))
				starStat[i] += amazingStarStat
			starStat[8] += amazingStarAtkSpl
			starStat[10] += amazingStarAtkSpl
			1
		} else if(!protected) {
			currentStar = 0
			canStarForce = true
			starStat[0] = 0; starStat[2] = 0;	starStat[4] = 0;	starStat[6] = 0
			starStat[8] = 0;	starStat[10] = 0; starStat[20] = 0
			maxStar = if(itemLvl >= 138) 25
			else if(itemLvl >= 128) 20
			else if(itemLvl >= 118) 15
			else if(itemLvl >= 108) 10
			else if(itemLvl >= 95) 8
			else 5
			amazing = 0
			-1 // 파괴
		} else 0
	}
}