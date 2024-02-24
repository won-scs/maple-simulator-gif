package com.maple_simulator_and_enchantor.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Log.w
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.maple_simulator_and_enchantor.AppData
import com.maple_simulator_and_enchantor.R
import com.maple_simulator_and_enchantor.database.*
import com.maple_simulator_and_enchantor.databinding.ActivityCharacterSelectBinding
import com.maple_simulator_and_enchantor.databinding.CharacterDisplayLayoutBinding
import kotlinx.coroutines.*

class CharacterSelectActivity : AppCompatActivity() {
	@SuppressLint("Recycle")
	var selectedNickname: String? = null
	var selectedJobName: String = ""
	private lateinit var binding: ActivityCharacterSelectBinding

	private lateinit var db: Database
	private lateinit var itemsDao: ItemsDao
	private lateinit var characterTableDao: CharacterTableDao
	private lateinit var characterTable: CharacterTable
	private lateinit var charactersDao: CharactersDao
	private lateinit var characters: List<Characters>
	private val maxCharacterNum = 40

	@SuppressLint("Recycle", "DiscouragedApi", "NotifyDataSetChanged")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityCharacterSelectBinding.inflate(layoutInflater)
		setContentView(binding.root)
		db = Database.getInstance(applicationContext)!!

		runBlocking {
			db = Database.getInstance(applicationContext)!!
			CoroutineScope(Dispatchers.IO).launch {
				itemsDao = db.itemsDao()
				characterTableDao = db.characterTableDao()
				charactersDao = db.charactersDao()
				characters = charactersDao.getAll()
			}.join()
		}

		createCharacterTable()

		class CharacterListViewHolder(val binding: CharacterDisplayLayoutBinding): RecyclerView.ViewHolder(binding.root)
		class CharacterListAdapter():
			RecyclerView.Adapter<RecyclerView.ViewHolder>(){
			override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
				return CharacterListViewHolder(CharacterDisplayLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
			}
			@SuppressLint("NotifyDataSetChanged")
			override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
				val bindingViewHolder = (holder as CharacterListViewHolder).binding
				if(position < characters.size) {
					if(selectedNickname == characters[position].nickname) {
						bindingViewHolder.background.setBackgroundResource(R.drawable.inventory_real_base2)
					} else {
						bindingViewHolder.background.setBackgroundColor(Color.TRANSPARENT)
					}
					bindingViewHolder.characterImage.visibility = View.VISIBLE
					bindingViewHolder.characterNameText.visibility = View.VISIBLE
					val dj = Glide.with(this@CharacterSelectActivity)
						.load("https://github.com/won-scs/maple-simulator/blob/main/${AppData.characterCodeList[characters[position].jobName]?.second}.gif?raw=true")
						.placeholder(resources.getIdentifier(AppData.characterCodeList[characters[position].jobName]?.second, "drawable", packageName))
					dj.into(bindingViewHolder.characterImage) // 캐릭터 GIF 애니메이션 넣기
					bindingViewHolder.characterNameText.text = characters[position].nickname
					bindingViewHolder.characterImage.setOnClickListener {
						selectedNickname = characters[position].nickname
						binding.CharacterSelectBtn.visibility = View.VISIBLE
						notifyDataSetChanged()
					}
				} else {
					bindingViewHolder.background.setBackgroundColor(Color.TRANSPARENT)
					bindingViewHolder.characterImage.visibility = View.INVISIBLE
					bindingViewHolder.characterNameText.visibility = View.INVISIBLE
				}
			}
			override fun getItemCount(): Int {
				return maxCharacterNum
			}
		}
		binding.cubeInfoTextList.layoutManager = GridLayoutManager(this, 2)
		val adapter = CharacterListAdapter()
		binding.cubeInfoTextList.adapter = adapter
		binding.CharacterSelectBtn.visibility = View.INVISIBLE
		selectedNickname = null
		binding.CharacterCreateBtn.isEnabled = characters.size != maxCharacterNum // 캐릭터 생성 버튼 활성화 여부

		val requestLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
			ActivityResultContracts.StartActivityForResult()
		) {
			binding.CharacterSelectBtn.visibility = View.INVISIBLE
			selectedNickname = null
			runBlocking {
				CoroutineScope(Dispatchers.IO).launch {
					characters = db.charactersDao().getAll()
				}.join()
			}
			binding.cubeInfoTextList.adapter = CharacterListAdapter()
			binding.CharacterCreateBtn.isEnabled = characters.size != maxCharacterNum // 캐릭터 생성 버튼 활성화 여부
		} // 캐릭터 생성 액티비티 끝나고 돌아왔을 때 실행될 Callback

		binding.CharacterCreateBtn.setOnClickListener {
			val intent = Intent(this, CharacterCreateActivity::class.java)
			requestLauncher.launch(intent)
		} // 캐릭터 생성 버튼 클릭했을 때

		binding.CharacterSelectBtn.setOnClickListener {
			val intent = Intent(this, MainScreenActivity::class.java)
			var selectedId = 0
			runBlocking {
				CoroutineScope(Dispatchers.IO).launch {
					selectedId = db.charactersDao().getByNickname(selectedNickname)[0].id
				}.join()
			}
			intent.putExtra("characterId", selectedId)
			requestLauncher.launch(intent)
		} // 캐릭터 선택 버튼 클릭했을 때

		binding.CharacterDeleteBtn.setOnClickListener {
			if (selectedNickname != null) {
				val dialog: AlertDialog.Builder = AlertDialog.Builder(this@CharacterSelectActivity)
				val eventHandler = DialogInterface.OnClickListener { p0, p1 ->
					if (p1 == DialogInterface.BUTTON_POSITIVE) {
						runBlocking {
							CoroutineScope(Dispatchers.IO).launch {
								val characterId = charactersDao.getByNickname(selectedNickname)[0].id
								charactersDao.deleteByNickname(selectedNickname!!)
								characters = charactersDao.getAll()
								itemsDao.updateResetWearCharacterId(characterId)
							}.join()
						}
						binding.CharacterSelectBtn.visibility = View.INVISIBLE
						selectedNickname = null
						binding.CharacterCreateBtn.isEnabled = characters.size != maxCharacterNum // 캐릭터 생성 버튼 활성화 여부
						binding.cubeInfoTextList.adapter = CharacterListAdapter()
					} // Yes를 눌렀을 때
					else if (p1 == DialogInterface.BUTTON_NEGATIVE) {
					} // No를 눌렀을 때
				}
				dialog.run {
					setMessage("정말 삭제하시겠습니까?")
					setPositiveButton("Yes", eventHandler)
					setNegativeButton("No", eventHandler)
					show()
				} // 캐릭터 삭제할건지 Dialog 띄우기
			}
		} // 캐릭터 삭제 버튼을 클릭했을 때
	}

	@SuppressLint("DiscouragedApi", "Recycle", "Range")

	private fun createCharacterTable() {
		val characterTables = emptyList<CharacterTable>().toMutableList()
		val characterTableString =
			"아델, " +
				"64, 15, 4, 0, 4, 0, 4, 0, 150, 10, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 115, 0, 28.0, 2, 14, 1000, 10, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 10, 45, 50, 90, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"1, 0, 0, 0, 0, 0, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"히어로, " +
				"84, 15, 34, 0, 4, 0, 4, 0, 80, 0, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"110, 110, 0, 58.0, 65, 24, 1500, 20, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"200, 0, 20, 25, 142, 90, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"1, 0, 0, 0, 0, 0, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"팔라딘, " +
				"54, 16, 34, 0, 4, 0, 4, 0, 110, 0, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"110, 110, 0, 58.27, 25, 0, 5500, 20, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"620, 250, 20, 47, 71, 91, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"1, 0, 0, 0, 0, 0, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"다크나이트, " +
				"54, 15, 34, 0, 4, 0, 4, 0, 100, 0, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"110, 110, 0, 30.0, 5, 0, 1500, 20, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"970, 0, 38, 55, 143, 90, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"1, 0, 0, 0, 0, 0, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"신궁, " +
				"34, 0, 134, 15, 4, 0, 4, 0, 125, 20, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"110, 110, 0, 58.0, 15, 0, 0, 40, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 30, 50, 69, 64, 85, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 1, 0, 0, 0, 1, 0, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"보우마스터, " +
				"34, 0, 134, 15, 4, 0, 4, 0, 150, 25, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"100, 130, 0, 55.0, 0, 0, 0, 40, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 30, 31, 69, 38, 85, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 1, 0, 0, 0, 1, 0, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"패스파인더, " +
				"34, 0, 134, 15, 4, 0, 4, 0, 80, 20, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"110, 110, 0, 30.0, 10, 0, 0, 50, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 30, 25, 89, 20, 85, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 1, 0, 0, 0, 1, 0, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"아크메이지(썬_콜), " +
				"4, 0, 4, 0, 64, 15, 4, 0, 0, 0, 70, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"110, 110, 0, 20.0, 50, 0, 0, 0, 0, 20, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"100, 0, 13, 35, 40, 95, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 0, 1, 0, 0, 0, 0, 0, 1, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"아크메이지(불_독), " +
				"4, 0, 4, 0, 64, 15, 4, 0, 0, 0, 70, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"110, 110, 0, 20.0, 50, 0, 0, 0, 750, 20, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"100, 0, 13, 35, 40, 95, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 0, 1, 0, 0, 0, 0, 0, 1, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"비숍, " +
				"4, 0, 4, 0, 94, 15, 4, 0, 0, 0, 120, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"100, 100, 0, 36.0, 40, 10, 0, 0, 750, 20, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"100, 0, 13, 75, 34, 95, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 0, 1, 0, 0, 0, 0, 0, 1, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"나이트로드, " +
				"4, 0, 34, 0, 4, 0, 74, 15, 140, 0, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 30.0, 0, 10, 0, 20, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 25, 75, 20, 85, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 0, 0, 1, 0, 0, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"섀도어, " +
				"4, 0, 34, 0, 4, 0, 84, 15, 160, 0, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 20.0, 25, 0, 0, 0, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 40, 55, 44, 90, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 0, 0, 1, 0, 1, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"듀얼블레이드, " +
				"4, 0, 4, 0, 4, 0, 54, 15, 90, 0, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 0.0, 10, 0, 0, 0, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 13, 40, 20, 90, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 0, 0, 1, 0, 1, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"바이퍼, " +
				"54, 15, 34, 0, 4, 0, 4, 0, 90, 19, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 40.0, 0, 20, 525, 20, 525, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 30, 40, 42, 90, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"1, 0, 0, 0, 0, 0, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"캡틴, " +
				"34, 0, 54, 15, 4, 0, 4, 0, 135, 20, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 20.0, 0, 0, 0, 20, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 55, 75, 30, 85, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 1, 0, 0, 0, 1, 0, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"캐논슈터, " +
				"144, 15, 124, 0, 4, 0, 4, 0, 60, 0, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 20.0, 0, 40, 0, 20, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 15, 45, 58, 85, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"1, 0, 0, 0, 0, 0, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"소울마스터, " +
				"134, 15, 54, 0, 4, 0, 4, 0, 180, 10, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 37.0, 0, 15, 1500, 0, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"750, 0, 15, 50, 35, 90, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"1, 0, 0, 0, 0, 0, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"윈드브레이커, " +
				"34, 0, 54, 30, 4, 0, 4, 0, 125, 20, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"110, 110, 0, 15.0, 35, 40, 1500, 20, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"1000, 0, 35, 60, 48, 85, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 1, 0, 0, 0, 1, 0, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"플레임위자드, " +
				"4, 0, 4, 0, 64, 15, 4, 0, 0, 0, 95, 10, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"110, 110, 0, 30.0, 0, 0, 0, 0, 0, 20, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"100, 0, 20, 35, 95, 95, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 0, 1, 0, 0, 0, 0, 0, 1, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"나이트워커, " +
				"4, 0, 4, 0, 4, 0, 64, 15, 65, 10, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 15.0, 30, 0, 0, 0, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 30, 40, 20, 85, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 0, 0, 1, 0, 0, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"스트라이커, " +
				"64, 15, 4, 0, 4, 0, 4, 0, 55, 10, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 45.0, 35, 0, 0, 20, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 45, 35, 42, 90, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"1, 0, 0, 0, 0, 0, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"아란, " +
				"54, 15, 34, 0, 4, 0, 4, 0, 180, 5, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 40.0, 40, 0, 0, 20, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 18, 55, 42, 90, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"1, 0, 0, 0, 0, 0, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"메르세데스, " +
				"34, 0, 54, 15, 4, 0, 4, 0, 170, 30, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"110, 110, 0, 25.0, 50, 20, 1500, 20, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"1000, 0, 30, 60, 98, 85, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 1, 0, 0, 0, 1, 0, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"에반, " +
				"4, 0, 4, 0, 54, 15, 4, 0, 0, 0, 150, 30, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"110, 110, 0, 20.0, 20, 20, 0, 0, 0, 20, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"100, 0, 40, 50, 51, 95, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 0, 1, 0, 0, 0, 0, 0, 1, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"루미너스, " +
				"4, 0, 4, 0, 64, 15, 4, 0, 0, 0, 110, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"110, 110, 0, 49.0, 55, 15, 0, 0, 0, 20, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"100, 0, 18, 40, 90, 95, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 0, 1, 0, 0, 0, 0, 0, 1, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"팬텀, " +
				"4, 0, 44, 0, 4, 0, 144, 15, 80, 0, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 30.0, 30, 0, 0, 0, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 15, 55, 72, 90, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 0, 0, 1, 0, 0, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"은월, " +
				"64, 15, 4, 0, 4, 0, 4, 0, 20, 4, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 30.0, 20, 30, 0, 20, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 45, 30, 33, 90, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"1, 0, 0, 0, 0, 0, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"블래스터, " +
				"34, 15, 34, 0, 4, 0, 4, 0, 60, 15, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 35.0, 20, 20, 0, 20, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 20, 35, 60, 90, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"1, 0, 0, 0, 0, 0, 1, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"와일드헌터, " +
				"94, 0, 154, 15, 4, 0, 4, 0, 100, 30, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"110, 110, 0, 30.0, 10, 10, 1500, 20, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"1000, 0, 55, 73, 32, 85, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 1, 0, 0, 0, 1, 0, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"배틀메이지, " +
				"4, 0, 4, 0, 44, 15, 4, 0, 0, 0, 80, 25, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"110, 110, 0, 30.0, 10, 0, 0, 0, 0, 20, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"100, 0, 35, 40, 25, 95, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 0, 1, 0, 0, 0, 0, 0, 1, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"제논, " +
				"109, 45, 109, 45, 4, 0, 109, 45, 80, 0, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 30.0, 40, 30, 0, 0, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 8, 45, 50, 90, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 0, 0, 0, 0, 0, 0, 0, 0, 0\n" + // 주스탯(앞5개), 부스탯(뒤5개)
				"메카닉, " +
				"4, 0, 34, 15, 4, 0, 4, 0, 75, 0, 0, 0, 0, 0, " + // 주스텟, 공, 마, 올스탯
				"120, 140, 0, 30.0, 55, 0, 0, 20, 0, 0, " + // 점프력, 이동속도, 착감, 방무, 데미지, 보뎀, HP, MP
				"0, 0, 5, 35, 33, 90, " + // 방어력, 방어력%, 크뎀, 크확, 최종뎀, 숙련도
				"0, 1, 0, 0, 0, 1, 0, 0, 0, 0"// 주스탯(앞5개), 부스탯(뒤5개)




		val readLines =  characterTableString.split("\n")
		for(line in readLines) {
			val words = line.split(", ")
			characterTables.add(
				CharacterTable(
				words[0].toString(),
				listOf(
					words[1].toInt(), words[2].toInt(), words[3].toInt(), words[4].toInt(), // 힘, 힘%, 덱, 덱%
					words[5].toInt(), words[6].toInt(), words[7].toInt(), words[8].toInt(), // 인트, 인트%, 럭, 럭%
					words[9].toInt(), words[10].toInt(), words[11].toInt(), words[12].toInt(), // 공격력, 공격력%, 마력, 마력%
					words[13].toInt(), words[14].toInt(), words[15].toInt(), words[16].toInt(), // 올스탯, 올스탯%, 점프력, 이동속도
					words[17].toInt(), 0, words[19].toInt(), words[20].toInt(), // 착감, 방무(0으로하자), 뎀, 보뎀
					words[21].toInt(), words[22].toInt(), words[23].toInt(), words[24].toInt(), // Hp, Hp%, Mp, Mp%
					words[25].toInt(), words[26].toInt(), 0, 0, // 방어력, 방어력%, 메획%, 아획%
					0,0,0,0,0,0, // 10렙당~
					words[27].toInt(),0, words[28].toInt(), // 크뎀, 쿨감, 크확
					0,0,0,
					0,0,0,0, 0,0,0,0, 0,0,
					0,0,0,0,0, words[29].toInt(),words[30].toInt(),0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0,
					0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0),
				words[18].toDouble(), listOf(words[31].toInt(), words[32].toInt(), words[33].toInt(), words[34].toInt(), words[35].toInt()), // 방무, 주스탯
					listOf(words[36].toInt(), words[37].toInt(), words[38].toInt(), words[39].toInt(), words[40].toInt())	// 부스탯
				)
			)
		}

		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				characterTableDao.insertAll(characterTables)
			}.join()
		}
	}
}