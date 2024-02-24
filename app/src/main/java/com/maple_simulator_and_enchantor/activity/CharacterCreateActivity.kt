package com.maple_simulator_and_enchantor.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import com.maple_simulator_and_enchantor.AppData
import com.maple_simulator_and_enchantor.R
import com.maple_simulator_and_enchantor.database.CharacterTable
import com.maple_simulator_and_enchantor.database.CharacterTableDao
import com.maple_simulator_and_enchantor.database.Characters
import com.maple_simulator_and_enchantor.database.Database
import com.maple_simulator_and_enchantor.databinding.ActivityCharacterCreateBinding
import kotlinx.coroutines.*

class CharacterCreateActivity : AppCompatActivity() {
	private lateinit var db: Database // 데이터베이스
	private lateinit var characterTableDao: CharacterTableDao
	private lateinit var characterTable: CharacterTable
	private lateinit var characters: List<Characters>
	private val nicknameList: MutableList<String> = emptyList<String>().toMutableList() // 닉네임 목록
	private lateinit var selectedJobName: String
	@SuppressLint("Recycle", "Range")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivityCharacterCreateBinding.inflate(layoutInflater)
		setContentView(binding.root)

		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				db = Database.getInstance(applicationContext)!!  // 데이터베이스 생성
				characters = db.charactersDao().getAll() // 데이터베이스에 있는 캐릭터들 정보 불러오기
				characterTableDao = db.characterTableDao()
			}.join()
		}
		for(character in characters) { nicknameList.add(character.nickname) } // 현재 있는 캐릭터들의 닉네임 목록 가져오기

		val jobNameList = resources.getStringArray(R.array.jobList)
		binding.jobList.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jobNameList)

		binding.jobList.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
			@SuppressLint("DiscouragedApi")
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				selectedJobName = jobNameList[position]
				val dj = Glide.with(this@CharacterCreateActivity)
					.load("https://github.com/won-scs/maple-simulator/blob/main/${AppData.characterCodeList[selectedJobName]?.second}.gif?raw=true")
					.placeholder(resources.getIdentifier(AppData.characterCodeList[selectedJobName]?.second, "drawable", packageName))
				dj.into(binding.skeletonAnimation) // 캐릭터 GIF 애니메이션 넣기
			}
			override fun onNothingSelected(parent: AdapterView<*>?) {
			}
		}

		binding.CharacterCreateConfirmBtn.setOnClickListener {
			val inputText: String = binding.CharacterNicknameInput.text.toString()
			if(inputText.length < 2) {
				Toast.makeText(this@CharacterCreateActivity, "닉네임은 2글자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}
			for(i in 0 until nicknameList.size) {
				if(inputText == nicknameList[i]) {
					Toast.makeText(this@CharacterCreateActivity, "중복된 닉네임입니다.", Toast.LENGTH_SHORT).show()
					return@setOnClickListener
				}
			} // 중복된 닉네임이면 메시지 띄우고 처음부터

			runBlocking {
				CoroutineScope(Dispatchers.IO).launch {
					characterTable = characterTableDao.getByJobName(selectedJobName)[0]
				}.join()
			}

			val newCharacter = Characters(0, characterTable.jobName, inputText,
					275, characterTable.baseStat,
					characterTable.baseArmIgn, 0, 0, 0, 0, 0, 0,
					characterTable.mainStat, characterTable.subStat) // 새 캐릭터 생성
			runBlocking {
				CoroutineScope(Dispatchers.IO).launch {
					db.charactersDao().insert(newCharacter)
				}.join() // 데이터베이스에 새 캐릭터 넣기 (코루틴 사용)
			}
			finish() // 종료
		} // 캐릭터 생성 확인 버튼 클릭 시

		binding.CharacterCreateCancelBtn.setOnClickListener {
			finish()
		} // 캐릭터 생성 취소 버튼 클릭 시
	}
}