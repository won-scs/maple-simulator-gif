package com.maple_simulator_and_enchantor.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.maple_simulator_and_enchantor.AppData
import com.maple_simulator_and_enchantor.Character
import com.maple_simulator_and_enchantor.database.*
import com.maple_simulator_and_enchantor.databinding.ActivityEquipmentBinding
import com.maple_simulator_and_enchantor.databinding.ActivityStatusBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class StatusActivity : AppCompatActivity() {
	lateinit var db: Database
	lateinit var binding: ActivityStatusBinding
	lateinit var cubeTables: List<CubeTable>
	lateinit var itemsDao: ItemsDao
	lateinit var characterData: Characters
	lateinit var itemTableDao: ItemTableDao
	var characterId = 0
	private val requestLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) {
		refresh()
	}
	private val setOptionTable = AppData.setOptionMade()

	@SuppressLint("DiscouragedApi")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		db = Database.getInstance(applicationContext)!!  // 데이터베이스 생성
		binding = ActivityStatusBinding.inflate(layoutInflater)
		characterId = intent.getIntExtra("characterId", 0)
		setContentView(binding.root)
		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				itemsDao = db.itemsDao()
				itemTableDao = db.itemTableDao()
				characterData = db.charactersDao().getByCharacterId(characterId)[0]
				cubeTables = db.cubeTableDao().getAll()
			}.join()
		}
		refresh()
//		when(characterData.jobName) {
//			"소울마스터", "윈드브레이커", "플레임위자드", "나이트워커", "스트라이커", "아델" -> binding.statusToDamageTestBtn.isEnabled = true;
//			else -> binding.statusToDamageTestBtn.isEnabled = false;
//		}
		val dj = Glide.with(this@StatusActivity)
			.load("https://github.com/won-scs/maple-simulator/blob/main/${AppData.characterCodeList[characterData.jobName]?.second}.gif?raw=true")
			.placeholder(resources.getIdentifier(AppData.characterCodeList[characterData.jobName]?.second, "drawable", packageName))
		dj.into(binding.characterImage) // 캐릭터 GIF 애니메이션 넣기
	}

	@SuppressLint("Recycle")
	fun characterStatText() {
		val textView: TextView = binding.characterStatText
		val builder: SpannableStringBuilder = SpannableStringBuilder("")
		val character = Character(characterData, itemsDao, itemTableDao, cubeTables, characterId)
		var mainStatString = ""
		if(character.jobName == "제논") mainStatString = "STR DEX LUK"
		else {
			if (character.mainStat[0].toInt() == 1) mainStatString += "STR "
			if (character.mainStat[1].toInt() == 1) mainStatString += "DEX "
			if (character.mainStat[2].toInt() == 1) mainStatString += "INT "
			if (character.mainStat[3].toInt() == 1) mainStatString += "LUK "
		}
		var subStatString = ""
		if(character.subStat[0].toInt() == 1) subStatString += "STR "
		if(character.subStat[1].toInt() == 1) subStatString += "DEX "
		if(character.subStat[2].toInt() == 1) subStatString += "INT "
		if(character.subStat[3].toInt() == 1) subStatString += "LUK "
		builder.append("주스탯: $mainStatString \n" +
			"부스탯: $subStatString\n\n")

		character.statAtkCalculate()


		val combatPower = character.combatCalculate()

		builder.append("전투력: ${combatPower.toLong()}\n\n")
		builder.append("스탯 공격력: ${character.statAtkFront.toLong()} ~ ${character.statAtkBack.toLong()}\n\n")
		builder.append(characterStatTextHelper("STR", character.totalStat[0], character.baseStat[0] + character.itemStat[0] + character.itemStat[12] + character.itemStat[28] * character.characterLvl / 10 ))
		builder.append(characterStatTextHelper("DEX", character.totalStat[2], character.baseStat[2] + character.itemStat[2] + character.itemStat[12] + character.itemStat[29] * character.characterLvl / 10 ))
		builder.append(characterStatTextHelper("INT", character.totalStat[4], character.baseStat[4] + character.itemStat[4] + character.itemStat[12] + character.itemStat[30] * character.characterLvl / 10 ))
		builder.append(characterStatTextHelper("LUK", character.totalStat[6], character.baseStat[6] + character.itemStat[6] + character.itemStat[12] + character.itemStat[31] * character.characterLvl / 10 ))
//		builder.append(characterStatTextHelper("최대 Hp", character.totalStat[20], character.baseStat[20] + character.itemStat[20]))
//		builder.append(characterStatTextHelper("최대 Mp", character.totalStat[20], character.baseStat[20] + character.itemStat[20]))


//		val detailStatTextView: TextView = binding.characterDetailStatText
//		val detailStatBuilder: SpannableStringBuilder = SpannableStringBuilder("")

		builder.append(characterStatTextHelper("크리티컬 데미지", character.totalStat[34], character.baseStat[34]))
//		builder.append(characterStatTextHelper("공격력", character.totalStat[8], character.baseStat[8] + character.itemStat[8]))
//		builder.append(characterStatTextHelper("마력", character.totalStat[10], character.baseStat[10] + character.itemStat[10]))
//		builder.append(characterStatTextHelper("방어력", character.totalStat[24], character.baseStat[24] + character.itemStat[24]))
//		builder.append(characterStatTextHelper("이동속도", character.totalStat[15]))
//		builder.append(characterStatTextHelper("점프력", character.totalStat[14]))
		builder.append(characterStatTextHelper("보스 몬스터 공격 시 데미지", character.totalStat[19]))
		builder.append(characterStatTextHelper("몬스터 방어율 무시", character.totalArmIgn))
		builder.append(characterStatTextHelper("데미지", character.totalStat[18]))
		builder.append(SpannableStringBuilder("아케인심볼 스탯: 13200\n어센틱심볼 스탯: 15000"))
		textView.text = builder
		var setOptionText = "적용 세트 효과\n공/마: +${character.setStat[8]} 올스탯: +${character.setStat[12]}\n" +
			"보공: +${character.setStat[19]}% 방무: +${String.format("%.2f", character.setArmIgn)}%"
		for(i in character.setOptionList.indices) {
			if(character.setOptionList[i].size >= 2) {
				for(setOption in setOptionTable) {
					if(setOption.key.first == i) {
						setOptionText += "\n" + setOption.key.second + ": ${character.setOptionList[i].size}세트"
					}
				}
			}
		}
		binding.characterSetText.text = setOptionText
	} // 장비 설명창

	private fun characterStatTextHelper(
		name: String, value: Int
	): SpannableStringBuilder {
		var baseText = "$name : $value"
		baseText = plusPercent(baseText, name)
		val returnString: SpannableStringBuilder = SpannableStringBuilder(baseText)
		returnString.append("\n")
		return returnString
	} // 장비 설명창 도움

	private fun characterStatTextHelper(
		name: String, value: Double
	): SpannableStringBuilder {
		var baseText = String.format("%s : %.2f", name, value)
		baseText = plusPercent(baseText, name)
		val returnString: SpannableStringBuilder = SpannableStringBuilder(baseText)
		returnString.append("\n")
		return returnString
	} // 장비 설명창 도움

	private fun characterStatTextHelper(
		name: String, totalStat: Int, baseStat: Int
	): SpannableStringBuilder {
		var baseText = "$name : $totalStat"
		baseText = plusPercent(baseText, name)
		baseText +=	" (${plusPercent(baseStat.toString(), name)} + ${plusPercent((totalStat-baseStat).toString(), name)})"
		val returnString: SpannableStringBuilder = SpannableStringBuilder(baseText)
		returnString.append("\n")
		return returnString
	} // 장비 설명창 도움

	private fun plusPercent(text: String, name: String): String {
		return if(name === "몬스터 방어율 무시" || name === "올스탯" || name === "데미지" ||
			name === "보스 몬스터 공격 시 데미지" || name === "크리티컬 데미지") "$text%"
		else text
	} // %를 써야하는 스텟들
	private fun refresh() {
		characterStatText()
	}
}