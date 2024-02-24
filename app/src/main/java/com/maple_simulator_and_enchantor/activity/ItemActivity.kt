package com.maple_simulator_and_enchantor.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_USER_ACTION
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Dimension
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.maple_simulator_and_enchantor.AppData
import com.maple_simulator_and_enchantor.Character
import com.maple_simulator_and_enchantor.Item
import com.maple_simulator_and_enchantor.R
import com.maple_simulator_and_enchantor.adapter_and_ui.SquareImageView
import com.maple_simulator_and_enchantor.database.*
import com.maple_simulator_and_enchantor.databinding.ActivityItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.w3c.dom.Text
import java.text.DecimalFormat

class ItemActivity : AppCompatActivity() {
	lateinit var binding: ActivityItemBinding
	lateinit var itItem: Item
	var characterId = 0
	lateinit var character: Character
	var itemId = 0
	private lateinit var aboutInventoryIntent: Intent
	private val requestLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) {
		refreshItItem()
		refreshWearItems()
		refreshStarImage()
		refreshStatAtkInfo()
		refreshUI()
	}


	lateinit var db: Database
	lateinit var itemsDao: ItemsDao
	lateinit var items: List<Items>
	lateinit var wearItems: List<Items>
	lateinit var itemTableDao: ItemTableDao
	lateinit var itemTables: List<ItemTable>
	lateinit var cubeTables: List<CubeTable>
	lateinit var itemTableData: ItemTable
	lateinit var charactersDao: CharactersDao
	lateinit var characterData: Characters
	val setOptionTable = AppData.setOptionMade()

	var starForceImageList = emptyList<ImageView>().toMutableList()

	@SuppressLint("Recycle", "DiscouragedApi", "Range")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityItemBinding.inflate(layoutInflater)
		setContentView(binding.root)
		db = Database.getInstance(applicationContext)!!  // 데이터베이스 생성
		aboutInventoryIntent = intent
		itemId = aboutInventoryIntent.getIntExtra("itemId", -1)
		characterId = aboutInventoryIntent.getIntExtra("characterId", 0)

		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				itemsDao = db.itemsDao()
				itemTableDao = db.itemTableDao()
				itemTableData = itemTableDao.getListByItemCode(itemsDao.getById(itemId)[0].itemCode)[0]
				cubeTables = db.cubeTableDao().getAll()
			}.join()
		}

		refreshItItem()
		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				charactersDao = db.charactersDao()
				characterData = charactersDao.getByCharacterId(characterId)[0]
				character = Character(characterData, itemsDao, itemTableDao, cubeTables, characterId)
			}.join()
		}

		refreshWearItems()
		refreshStatAtkInfo()
		refreshWearOrTakeOffBtn()
		refreshUI()

		val canWearBuilder = SpannableStringBuilder("")
		val jobType = listOf("전사", "궁수", "마법사", "도적", "해적")
		for(i in jobType.indices) {
			val wearString = SpannableStringBuilder(" ${jobType[i]} ")
			if((i+1 == itItem.itemType / 100) || (6 == itItem.itemType / 100)) {
				wearString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.unique_yellow)), 0, wearString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
			}
			canWearBuilder.append(wearString)
		}
		if(itItem.itemCode/10000 >= 10) {
			val jikupgoon = SpannableStringBuilder("\n${AppData.characterCodeList.filter { itItem.itemCode / 10000 - 10 == it.value.first[0] }.keys.first()} 직업군 사용 가능")
			jikupgoon.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.unique_yellow)), 0, jikupgoon.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
			canWearBuilder.append(jikupgoon)
		}
		binding.equipTypeJobText.text = canWearBuilder
		try {
			if (itItem.itemType % 100 != 0) {
				if (itItem.itemType % 100 == 21) {
						binding.equipmentWearBtn.isEnabled =
							(character.jobName == "아크메이지(썬_콜)" ||
								character.jobName == "아크메이지(불_독)" ||
								character.jobName == "비숍" ||
								character.jobName == "플레임위자드" ||
								character.jobName == "섀도어") &&
								(AppData.characterCodeList[character.jobName]?.first?.get(1) == itItem.itemCode / 10000)
				} else {
					binding.equipmentWearBtn.isEnabled =
						!(AppData.characterCodeList[character.jobName]?.first?.get(0) != itItem.itemCode / 10000 - 10 &&
							(AppData.characterCodeList[character.jobName]?.first?.get(1) != itItem.itemCode / 10000) &&
							(itItem.itemCode / 10000 != 6) && (AppData.characterCodeList[character.jobName]?.first?.get(
							2
						) != itItem.itemCode / 10000 - 10))
				}
			} else {
				binding.equipmentWearBtn.isEnabled =
					AppData.itemJob[itItem.name]?.first?.contains(character.jobName) == true
			}
		} catch (e: Exception) {
			binding.equipmentWearBtn.isEnabled = false
		}

		for(i in 1..5) {
			val view: ImageView = findViewById(resources.getIdentifier(
				"starForceImg$i", "id", packageName
			))
			starForceImageList.add(view)
		}
		refreshStarImage()

		val imageView: SquareImageView = binding.itemActivityItemImage
		imageView.setImageResource(
			resources.getIdentifier("id${itItem.itemCode}", "drawable", packageName)
		)

		binding.itemTooptionChoiceBtn.setOnClickListener {
			val intent: Intent = Intent(this, ExtraOptionActivity::class.java)
			intent.putExtra("itemId", itItem.itemId)
			intent.addFlags(FLAG_ACTIVITY_NO_USER_ACTION)
			requestLauncher.launch(intent)
		} // 추가옵션 버튼을 눌렀을 때

		binding.itemToStarForceBtn.setOnClickListener {
			val intent: Intent = Intent(this, StarForceActivity::class.java)
			intent.putExtra("itemId", itItem.itemId)
			requestLauncher.launch(intent)
		} // 스타포스 버튼을 눌렀을 때

		binding.itemToPotentialBtn.setOnClickListener {
			val intent: Intent = Intent(this, PotentialAbilityActivity::class.java)
			intent.putExtra("itemId", itItem.itemId)
			intent.addFlags(FLAG_ACTIVITY_NO_USER_ACTION)
			requestLauncher.launch(intent)
		} // 잠재능력 버튼을 눌렀을 때

		binding.itemToEnchantBtn.setOnClickListener {
			val intent: Intent = Intent(this, EnchantActivity::class.java)
			intent.putExtra("itemId", itItem.itemId)
			requestLauncher.launch(intent)
		} // 잠재능력 버튼을 눌렀을 때

		binding.equipmentWearBtn.setOnClickListener {
			val pendant_count = emptyList<Int>().toMutableList()
			val ring_count = emptyList<Int>().toMutableList()
			runBlocking {
				CoroutineScope(Dispatchers.IO).launch {
					characterData = charactersDao.getByCharacterId(characterId)[0]
					itemsDao = db.itemsDao()
					items = itemsDao.getByWearCharacterId(characterId)
				}.join()
			}
			pendant_count.add(characterData.pendant1Id)
			pendant_count.add(characterData.pendant2Id)
			ring_count.add(characterData.ring1Id)
			ring_count.add(characterData.ring2Id)
			ring_count.add(characterData.ring3Id)
			ring_count.add(characterData.ring4Id)

			if(itItem.itemType % 100 == 18) {
				var wherePendent = 0
				for(i in 0 .. 1) {
					if(pendant_count[i] != 0) {
						var wearItemCode = 0
						runBlocking {
							CoroutineScope(Dispatchers.IO).launch {
								wearItemCode = try {
									itemsDao.getById(pendant_count[i])[0].itemCode
								} catch (e: Exception) {
									0
								}
							}.join()
						}
						if(wearItemCode == itItem.itemCode) {
							wherePendent = i + 1
							break
						} // 같은 종류의 펜던트를 이미 끼고 있다면
					} 
				}
				for(i in 0 .. 1) {
					if(wherePendent == 0 && pendant_count[i] == 0) {
						wherePendent = i + 1
						break
					} // 겹치는 펜던트가 없고, 자리가 비어있는 곳이 있다면
				}
				if(wherePendent == 0) wherePendent = 1 // 겹치는 펜던트 없고, 자리가 비어있는 곳이 없다면
				if(characterData.getPendantId(wherePendent) != 0) {
					runBlocking {
						CoroutineScope(Dispatchers.IO).launch {
							itemsDao.updateWearCharacterId(0, characterData.getPendantId(wherePendent))
						}.join()
					}
				} // 낄 자리에 펜던트가 이미 있으면 있던 곳 wearCharacterId 0으로
				val pendantRingIdList = mutableListOf<Int>(characterData.pendant1Id, characterData.pendant2Id,
				characterData.ring1Id, characterData.ring2Id, characterData.ring3Id, characterData.ring4Id)
				pendantRingIdList[wherePendent-1] = itItem.itemId

				runBlocking {
					CoroutineScope(Dispatchers.IO).launch {
						charactersDao.updatePendantRingId(pendantRingIdList[0], pendantRingIdList[1], pendantRingIdList[2],
							pendantRingIdList[3], pendantRingIdList[4], pendantRingIdList[5], characterId)
					}.join()
				} // 펜던트 착용
			} // 펜던트라면
			else if(itItem.itemType % 100 == 17) {
				var whereRing = 0
				for(i in 0 .. 3) {
					if(ring_count[i] != 0) {
						var wearItemCode = 0
						runBlocking {
							CoroutineScope(Dispatchers.IO).launch {
								wearItemCode = try {
									itemsDao.getById(ring_count[i])[0].itemCode
								} catch (e: Exception) {
									0
								}
							}.join()
						}
						if(wearItemCode == itItem.itemCode) {
							whereRing = i + 1
							break
						} // 같은 종류의 펜던트를 이미 끼고 있다면
					}
				}
				for(i in 0 .. 3) {
					if(whereRing == 0 && ring_count[i] == 0) {
						whereRing = i + 1
						break
					} // 겹치는 펜던트가 없고, 자리가 비어있는 곳이 있다면
				}
				if(whereRing == 0) whereRing = 1 // 겹치는 펜던트 없고, 자리가 비어있는 곳이 없다면
				if(characterData.getRingId(whereRing) != 0) {
					runBlocking {
						CoroutineScope(Dispatchers.IO).launch {
							itemsDao.updateWearCharacterId(0, characterData.getRingId(whereRing))
						}.join()
					}
				} // 낄 자리에 펜던트가 이미 있으면 있던 곳 wearCharacterId 0으로
				val pendantRingIdList = mutableListOf<Int>(characterData.pendant1Id, characterData.pendant2Id,
					characterData.ring1Id, characterData.ring2Id, characterData.ring3Id, characterData.ring4Id)
				pendantRingIdList[whereRing - 1 + 2] = itItem.itemId
				runBlocking {
					CoroutineScope(Dispatchers.IO).launch {
						charactersDao.updatePendantRingId(pendantRingIdList[0], pendantRingIdList[1], pendantRingIdList[2],
							pendantRingIdList[3], pendantRingIdList[4], pendantRingIdList[5], characterId)
					}.join()
				} // 반지 착용
			} // 반지라면
			else {
				for(item in items) {
					val wearItemType = item.itemType
					if(wearItemType % 100 == itItem.itemType % 100 ||
						(wearItemType % 100 == 1 && itItem.itemType % 100 == 21) ||
						(wearItemType % 100 == 21 && itItem.itemType % 100 == 1)) {
						runBlocking {
							CoroutineScope(Dispatchers.IO).launch {
								itemsDao.updateWearCharacterId(0, item.id)
							}.join()
						}
						break
					}
				}
			}
			runBlocking {
				CoroutineScope(Dispatchers.IO).launch {
					itemsDao.updateWearCharacterId(characterId, itItem.itemId)
				}.join()
			}
			refreshItItem()
			refreshWearItems()
			refreshStatAtkInfo()
			refreshWearOrTakeOffBtn()
			refreshUI()
		} // 착용 버튼을 눌렀을 때

		binding.equipmentTakeOffBtn.setOnClickListener {
			if(itItem.itemType % 100 == 18) {
				for(i in 1 .. 2) {
					if (characterData.getPendantId(i) == itItem.itemId) {
						val pendantRingIdList = mutableListOf<Int>(characterData.pendant1Id, characterData.pendant2Id,
							characterData.ring1Id, characterData.ring2Id, characterData.ring3Id, characterData.ring4Id)
						pendantRingIdList[i-1] = 0
						runBlocking {
							CoroutineScope(Dispatchers.IO).launch {
								charactersDao.updatePendantRingId(pendantRingIdList[0], pendantRingIdList[1], pendantRingIdList[2],
									pendantRingIdList[3], pendantRingIdList[4], pendantRingIdList[5], characterId)
							}.join()
						} // 펜던트 해제
					}
				}
			} else if(itItem.itemType % 100 == 17) {
				for(i in 1 .. 4) {
					if (characterData.getRingId(i) == itItem.itemId) {
						val pendantRingIdList = mutableListOf<Int>(characterData.pendant1Id, characterData.pendant2Id,
							characterData.ring1Id, characterData.ring2Id, characterData.ring3Id, characterData.ring4Id)
						pendantRingIdList[i-1 + 2] = 0
						runBlocking {
							CoroutineScope(Dispatchers.IO).launch {
								charactersDao.updatePendantRingId(pendantRingIdList[0], pendantRingIdList[1], pendantRingIdList[2],
									pendantRingIdList[3], pendantRingIdList[4], pendantRingIdList[5], characterId)
							}.join()
						} // 반지 해제
					}
				}
			}
			runBlocking {
				CoroutineScope(Dispatchers.IO).launch {
					characterData = charactersDao.getByCharacterId(characterId)[0]
					itemsDao.updateWearCharacterId(0, itItem.itemId)
				}.join()
			}
			refreshItItem()
			refreshWearItems()
			refreshStatAtkInfo()
			refreshWearOrTakeOffBtn()
			refreshUI()
		} // 해제 버튼을 눌렀을 때

		binding.equipmentDeleteBtn.setOnClickListener {
			val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
			dialog.run {
				val eventHandler = DialogInterface.OnClickListener { _, p1 ->
					if (p1 == DialogInterface.BUTTON_POSITIVE) {
						runBlocking {
							CoroutineScope(Dispatchers.IO).launch {
								itemsDao.deleteById(itItem.itemId)
							}.join()
						}
						finish()
					} // Yes를 눌렀을 때
					else if (p1 == DialogInterface.BUTTON_NEGATIVE) {
						Log.d("wow", "good choice")
					} // No를 눌렀을 때
				}
				setMessage("정말 삭제하시겠습니까?")
				setPositiveButton("Yes", eventHandler)
				setNegativeButton("No", eventHandler)
				show()
			} // 장비 정말 삭제할건지 Dialog 띄우기
		} // 장비 삭제 버튼을 눌렀을 때
	}

	private fun equipInfoText() {
		val textView: TextView = binding.equipInfoText
		var builder: SpannableStringBuilder = SpannableStringBuilder("")
		builder.append(SpannableStringBuilder("장비분류 : ${itItem.itemTypeNameList[itItem.itemType%100]}\n"))
		builder.append(equipInfoTextHelper("STR", itItem.baseStat[0], itItem.extraStat[0], itItem.starStat[0], itItem.enchantStat[0]))
		builder.append(equipInfoTextHelper("DEX", itItem.baseStat[2], itItem.extraStat[2], itItem.starStat[2], itItem.enchantStat[2]))
		builder.append(equipInfoTextHelper("INT", itItem.baseStat[4], itItem.extraStat[4], itItem.starStat[4], itItem.enchantStat[4]))
		builder.append(equipInfoTextHelper("LUK", itItem.baseStat[6], itItem.extraStat[6], itItem.starStat[6], itItem.enchantStat[6]))
		builder.append(equipInfoTextHelper("최대 Hp", itItem.baseStat[20], itItem.extraStat[20], itItem.starStat[20], itItem.enchantStat[20]))
		builder.append(equipInfoTextHelper("최대 Mp", itItem.baseStat[22], itItem.extraStat[22], itItem.starStat[22], itItem.enchantStat[22]))
		builder.append(equipInfoTextHelper("공격력", itItem.baseStat[8], itItem.extraStat[8], itItem.starStat[8], itItem.enchantStat[8]))
		builder.append(equipInfoTextHelper("마력", itItem.baseStat[10], itItem.extraStat[10], itItem.starStat[10], itItem.enchantStat[10]))
		builder.append(equipInfoTextHelper("방어력", itItem.baseStat[24], itItem.extraStat[24], itItem.starStat[24], itItem.enchantStat[24]))
		builder.append(equipInfoTextHelper("이동속도", itItem.baseStat[15], itItem.extraStat[15], itItem.starStat[15], itItem.enchantStat[15]))
		builder.append(equipInfoTextHelper("점프력", itItem.baseStat[14], itItem.extraStat[14], itItem.starStat[14], itItem.enchantStat[14]))
		builder.append(equipInfoTextHelper("보스 몬스터 공격 시 데미지", itItem.baseStat[19], itItem.extraStat[19], itItem.starStat[19], itItem.enchantStat[19]))
		builder.append(equipInfoTextHelper("몬스터 방어율 무시", itItem.baseArmIgn.toInt(), itItem.extraArmIgn.toInt(), itItem.starArmIgn.toInt(), itItem.enchantArmIgn.toInt()))
		builder.append(equipInfoTextHelper("데미지", itItem.baseStat[18], itItem.extraStat[18], itItem.starStat[18], itItem.enchantStat[18]))
		builder.append(equipInfoTextHelper("올스탯", itItem.baseStat[13], itItem.extraStat[13], itItem.starStat[13], itItem.enchantStat[13]))
		builder.append(equipInfoTextHelper("착용 제한 레벨 감소", itItem.baseStat[16], itItem.extraStat[16], itItem.starStat[16], itItem.enchantStat[16]))
		if(itItem.canEnchant) {
			val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.unique_yellow))
			builder.append("업그레이드 가능 횟수: ${itItem.remainUpGrade}")
			val restoreString = SpannableStringBuilder(" (복구 가능 횟수 : ${itItem.upGradeRestore})\n")
			restoreString.setSpan(colorSpan, 0, restoreString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
			builder.append(restoreString)
			if(itItem.goldHammer == 1) {
				builder.append("황금망치 제련 적용\n")
			}
		}

		builder.delete(builder.length-1, builder.length)
		textView.text = builder

		val equipAbilityText: TextView = binding.equipAbilityText
		builder = SpannableStringBuilder("")
		builder.append(abilityGrade(itItem.upAbility[0], 0))
		builder.append(AppData.potentialAbilityTextHelper(itItem.upAbility[2]))
		builder.append(AppData.potentialAbilityTextHelper(itItem.upAbility[3]))
		builder.append(AppData.potentialAbilityTextHelper(itItem.upAbility[4]))
		builder.append("\n")
		builder.append(abilityGrade(itItem.downAbility[0], 1))
		builder.append(AppData.potentialAbilityTextHelper(itItem.downAbility[2]))
		builder.append(AppData.potentialAbilityTextHelper(itItem.downAbility[3]))
		builder.append(AppData.potentialAbilityTextHelper(itItem.downAbility[4]))
		builder.delete(builder.length-1, builder.length)

		equipAbilityText.text = builder
	} // 장비 설명창

	private fun abilityGrade(grade: Int, upOrdown: Int): SpannableStringBuilder {
		var returnString: SpannableStringBuilder = SpannableStringBuilder()
		when(grade) {
			1 -> {
				returnString = SpannableStringBuilder("레어 ")
				returnString.append(when(upOrdown){0 -> "잠재옵션\n"; else -> "에디셔널 잠재옵션\n"})
				returnString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.rare_blue)), 0, returnString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
			}
			2 -> {
				returnString = SpannableStringBuilder("에픽 ")
				returnString.append(when(upOrdown){0 -> "잠재옵션\n"; else -> "에디셔널 잠재옵션\n"})
				returnString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.epic_purple)), 0, returnString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
			}
			3 -> {
				returnString = SpannableStringBuilder("유니크 ")
				returnString.append(when(upOrdown){0 -> "잠재옵션\n"; else -> "에디셔널 잠재옵션\n"})
				returnString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.unique_yellow)), 0, returnString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
			}
			4 -> {
				returnString = SpannableStringBuilder("레전드리 ")
				returnString.append(when(upOrdown){0 -> "잠재옵션\n"; else -> "에디셔널 잠재옵션\n"})
				returnString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.legendary_green)), 0, returnString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
			}
		}
		return returnString
	}

	private fun equipInfoTextHelper(
		name: String, baseValue: Int, extraValue: Int,
		starValue: Int, enchantValue: Int
	): SpannableStringBuilder {
		val totalValue = baseValue + extraValue + starValue + enchantValue
		if (totalValue == 0)
			return SpannableStringBuilder("")
		var baseText = "$name : +$totalValue"
		baseText = plusPercent(baseText, name)
		var returnString: SpannableStringBuilder = SpannableStringBuilder(baseText)
		if (totalValue != baseValue) {
			returnString.append(" (${baseValue}")
			if (extraValue != 0) {
				val colorExtraSpan = ForegroundColorSpan(Color.GREEN)
				if(name == "착용 제한 레벨 감소") {
					returnString = SpannableStringBuilder("$name : -$totalValue\n")
					returnString.setSpan(colorExtraSpan, 0,
						returnString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
					return returnString
				}
				var text = "+${extraValue}"
				text = plusPercent(text, name)
				returnString.append(text)
				returnString.setSpan(colorExtraSpan, returnString.length - text.length,
					returnString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
			}
			if (starValue != 0) {
				val colorStarSpan = ForegroundColorSpan(Color.rgb(255, 130, 0))
				var text = "+${starValue}"
				text = plusPercent(text, name)
				returnString.append(text)
				returnString.setSpan(colorStarSpan, returnString.length - text.length,
					returnString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
			}
			if (enchantValue != 0) {
				val colorEnchatSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.rare_blue))
				var text = "+${enchantValue}"
				text = plusPercent(text, name)
				returnString.append(text)
				returnString.setSpan(colorEnchatSpan, returnString.length - text.length,
					returnString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
			}
			returnString.append(")")
		}
		returnString.append("\n")
		return returnString
	} // 장비 설명창 도움

	private fun plusPercent(text: String, name: String): String {
		return if(name === "몬스터 방어율 무시" || name === "올스탯" || name === "데미지" ||
			name === "보스 몬스터 공격 시 데미지") "$text%"
		else text
	} // %를 써야하는 스텟들

	private fun refreshItItem() {
		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				val itemData = itemsDao.getById(itemId)[0]
				itItem = Item(itemsDao, itemData, cubeTables)
			}.join()
		}
	}

	private fun refreshWearOrTakeOffBtn() {
		if(itItem.wearCharacterId == characterId) {
			binding.equipmentWearBtn.visibility = View.INVISIBLE
			binding.equipmentTakeOffBtn.visibility = View.VISIBLE
		} else {
			binding.equipmentWearBtn.visibility = View.VISIBLE
			binding.equipmentTakeOffBtn.visibility = View.INVISIBLE
		} // 착용 버튼 또는 착용 해제 버튼 띄우기
	}

	private fun refreshWearItems() {
		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				wearItems = itemsDao.getByWearCharacterId(characterId)
			}.join()
		}
	}

	@SuppressLint("SetTextI18n")
	private fun refreshStatAtkInfo() {
		if(itItem.wearCharacterId == characterId) {
			binding.statAtkText.text = "+0"
			return
		}
		val wearThisCharacter = Character(characterData, itemsDao, itemTableDao, cubeTables, characterId) // 스공 증가량 계산을 위한 캐릭터
		wearThisCharacter.statAtkCalculate()
		val beforeStatAtk = wearThisCharacter.statAtkBack
		for(i in 0 until 100) {
			wearThisCharacter.itemStat[i] += itItem.totalStat[i]
		}
//		if(itItem.setCode != 0)
//			wearThisCharacter.setOptionList[itItem.setCode].add(1)
		for(wearItem in wearItems) {
			val imsiItem = Item(itemsDao, wearItem, cubeTables)
			val wearItemType = wearItem.itemType
			if(wearItemType%100 == itItem.itemType%100) {
				if((itItem.itemType % 100 == 18 && wearItem.id == wearThisCharacter.pendantId[0]) ||
					(itItem.itemType % 100 == 17 && wearItem.id == wearThisCharacter.ringId[0]) ||
					(itItem.itemType % 100 != 18 || itItem.itemType % 100 != 17)) {
						for(i in 0 until 100) {
							wearThisCharacter.itemStat[i] -= imsiItem.totalStat[i]
						}
//					if(wearItem.itemSetCode != 0) {
//						if(wearItem.itemSetCode != 777) {
//							Log.d("wearItem itemSetCode", "${wearItem.name}: ${wearItem.itemSetCode}")
//							wearThisCharacter.setOptionList[wearItem.itemSetCode].removeAt(0)
//						}
//						else {
//							for(setOption in setOptionTable) {
//								if(setOption.value.first.contains(wearItem.itemType%100)) {
//									if(wearThisCharacter.setOptionList[setOption.key.first].size != 0)
//										wearThisCharacter.setOptionList[setOption.key.first].removeAt(0)
//								} // 그 럭키아이템의 아이템 종류가 현 아이템의 종류와 같다면
//							}
//						} // 럭키아이템이면
//					}
				}
			}
		}
		if(itItem.itemType % 100 == 0) wearThisCharacter.weaponConstant = AppData.itemJob[itItem.name]?.second!!
		wearThisCharacter.statAtkCalculate()
		wearThisCharacter.weaponConstant = 0.0
		val afterStatAtk = wearThisCharacter.statAtkBack
		if((afterStatAtk - beforeStatAtk) >= 0)
			binding.statAtkText.text = "+" + (afterStatAtk - beforeStatAtk).toInt().toString()
		else
			binding.statAtkText.text = (afterStatAtk - beforeStatAtk).toInt().toString()
	}

	@SuppressLint("DiscouragedApi")
	private fun refreshStarImage() {
		for(i in 0..4) {
			val view: ImageView = starForceImageList[i]
			view.setImageResource(resources.getIdentifier("star0_5", "drawable", packageName))
			view.visibility = View.INVISIBLE
		} // 별 투명화
		if(itItem.maxStar != 0) {
			val starImageView: ImageView
			val blueStar = when(itItem.amazing) {1 -> "blue"; else -> ""}
			Log.d("bluestar", blueStar)
			if(itItem.maxStar <= 10) {
				for(i in 1..(itItem.maxStar+4)/5) {
					val view: ImageView = findViewById(resources.getIdentifier(
						"starForceImg${i+3}", "id", packageName
					))
					if(i == (itItem.maxStar+4)/5 && itItem.maxStar == 8)
						view.setImageResource(resources.getIdentifier("star${blueStar}0_3", "drawable", packageName))
					view.visibility = View.VISIBLE
				}
				 starImageView = if(itItem.currentStar >= 5) {
					binding.starForceImg4.setImageResource(
						resources.getIdentifier("star${blueStar}5_5", "drawable", packageName))
					binding.starForceImg5
				} else binding.starForceImg4
			} else {
				for(i in 1..(itItem.maxStar+4)/5) {
					val view: ImageView = starForceImageList[i-1]
					view.visibility = View.VISIBLE
				}
				for(i in 1 .. (itItem.currentStar)/5) {
					starForceImageList[i-1].setImageResource(resources.getIdentifier("star${blueStar}5_5", "drawable", packageName))
				}
				starImageView = if(itItem.currentStar < 5) binding.starForceImg1
				else if(itItem.currentStar < 10) binding.starForceImg2
				else if(itItem.currentStar < 15) binding.starForceImg3
				else if(itItem.currentStar < 20) binding.starForceImg4
				else binding.starForceImg5
			}
			if(itItem.currentStar % 5 != 0) {
				if(itItem.maxStar == 8 && itItem.currentStar > 5)
					starImageView.setImageResource(resources.getIdentifier("star${blueStar}${itItem.currentStar%5}_3", "drawable", packageName))
				else
					starImageView.setImageResource(resources.getIdentifier("star${blueStar}${itItem.currentStar%5}_5", "drawable", packageName))
			}
		} // 별 개수 맞춰서 띄우기
	}

	@SuppressLint("SetTextI18n", "DiscouragedApi", "SuspiciousIndentation")
	private fun refreshUI() {
		if(itItem.upGrade - itItem.remainUpGrade - itItem.upGradeRestore + itItem.goldHammer != 0) {
			val nameText =itemTableData.name + " (+${itItem.upGrade - itItem.remainUpGrade - itItem.upGradeRestore + itItem.goldHammer})"
			binding.itemNameText.text = nameText
			if(nameText.length >= 12)
				binding.itemNameText.setTextSize(Dimension.SP, 25F)
		}
		else {
			binding.itemNameText.setTextSize(Dimension.SP, 32F)
			binding.itemNameText.text = itemTableData.name
		}
		binding.itemTooptionChoiceBtn.isEnabled = itItem.canFlame // 추가옵션 설정 불가 아이템들
		binding.itemToStarForceBtn.isEnabled = itItem.canStarForce // 스타포스 버튼 활성화 여부
		binding.itemToPotentialBtn.isEnabled = itItem.canCube // 큐브 버튼 활성화 여부
		binding.itemToEnchantBtn.isEnabled = itItem.canEnchant // 주문서 버튼 활성화 여부
		binding.equipAbilityText.visibility = when(itItem.canCube) {
			true -> View.VISIBLE; false -> {
				if(itItem.name == "블랙 하트") View.VISIBLE
				else View.INVISIBLE
			}
		} // 큐브 사용 불가 템이면 잠재옵션 설명 부분 가리기
		equipInfoText()
		val deciFormat = DecimalFormat("#,###")
		val cashItemNameList = listOf("", "레드 큐브", "블랙 큐브", "에디셔널 큐브", "화이트 에디셔널 큐브", "장인의 큐브", "", "", "", "", "",
			"영원한 환생의 불꽃", "강력한 환생의 불꽃", "검은 환생의 불꽃", "", "", "", "", "", "", "",
			"놀라운 긍정의 혼돈 주문서", "이노센트 주문서", "순백의 주문서", "황금 망치", "매지컬 주문서", "", "놀라운 장비강화 주문서", "", "", "리턴 스크롤")
		var useText = "누적 사용 메소: ${deciFormat.format(itItem.useMeso)}메소\n" +
			"누적 파괴 횟수: ${deciFormat.format(itItem.destroyNum)}회\n"
		for(i in cashItemNameList.indices) {
			if(itItem.useCash[i] != 0 && cashItemNameList[i] != "")
			useText += "${cashItemNameList[i]} : ${deciFormat.format(itItem.useCash[i])}개\n"
		}
		useText = useText.substring(0 until useText.length-1)
		binding.equipUseMoneyText.text =  useText

		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				characterData = charactersDao.getByCharacterId(characterId)[0]
				character = Character(characterData, itemsDao, itemTableDao, cubeTables, characterId)
			}.join()
		}

		var setOptionText = ""
		for(i in character.setOptionList.indices) {
			if(character.setOptionList[i].size >= 2) {
				for(setOption in setOptionTable) {
					if(setOption.key.first == i) {
						if(setOptionText != "") setOptionText += "\n"
						setOptionText += setOption.key.second +
							": ${character.setOptionList[i].size}세트 (${character.setOptionList[i].size}/${setOption.value.first.size})"
					}
				}
			}
		}

		binding.setOptionText.text = setOptionText
	}
}