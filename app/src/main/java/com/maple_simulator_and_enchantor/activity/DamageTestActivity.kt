package com.maple_simulator_and_enchantor.activity

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.maple_simulator_and_enchantor.AppData
import com.maple_simulator_and_enchantor.Boss
import com.maple_simulator_and_enchantor.BossInfo
import com.maple_simulator_and_enchantor.Character
import com.maple_simulator_and_enchantor.database.*
import com.maple_simulator_and_enchantor.databinding.ActivityDamageTestBinding
import com.maple_simulator_and_enchantor.databinding.DamageLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import kotlin.concurrent.timer

class DamageTestActivity : AppCompatActivity() {
	lateinit var db: Database
	lateinit var binding: ActivityDamageTestBinding
	lateinit var cubeTables: List<CubeTable>
	lateinit var itemsDao: ItemsDao
	lateinit var characterData: Characters
	lateinit var itemTableDao: ItemTableDao
	var characterId = 0; var bossId = 0
	var bossHP = 1500000000000L
	var bossMaxHP = 1500000000000L
	private val skillMap: Map<Pair<String, Int>, List<Any>> // key: 직업이름, 스킬이름, value: 수치, 타수, 딜레이, 쿨타임
		= mapOf(Pair(Pair("소울마스터", 0), listOf("솔라 슬래시 VI", listOf(830, 830, 830, 830, 830), 720, 0)),
		Pair(Pair("윈드브레이커", 0), listOf("천공의 노래 VI", listOf(960), 180, 0)),
		Pair(Pair("플레임위자드", 0), listOf("인피니티 플레임 서클", listOf(1100, 1100, 1100, 1100, 1100, 1100, 1100, 1100), 360, 2500)),
		Pair(Pair("나이트워커", 0), listOf("퀸터플 스로우 VI", listOf(330, 330, 330, 330, 300, 300, 300, 300, 300, 300, 300), 450, 0)),
		Pair(Pair("스트라이커", 0), listOf("섬멸 VI", listOf(470, 470, 470, 470, 470, 470, 470), 780, 0)),
		Pair(Pair("아델", 0), listOf("디바이드 VI", listOf(750, 750, 750, 750, 750, 750), 780, 0)),
		Pair(Pair("히어로", 0), listOf("레이징 블로우 VI", listOf(590, 590, 590, 590), 780, 0)),
		Pair(Pair("팔라딘", 0), listOf("블래스트 VI", listOf(465, 465, 465, 465, 465, 465, 465, 465, 465, 465), 780, 0)),
		Pair(Pair("다크나이트", 0), listOf("궁니르 디센트 VI", listOf(390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390), 780, 0)),
		Pair(Pair("신궁", 0), listOf("스나이핑 VI", listOf(545, 545, 545, 545, 545, 545, 545, 545, 545), 780, 0)),
		Pair(Pair("보우마스터", 0), listOf("폭풍의 시 VI", listOf(750), 180, 0)),
		Pair(Pair("패스파인더", 0), listOf("카디널 블래스트 VI", listOf(930, 930, 930, 930, 930), 480, 0)),
		Pair(Pair("아크메이지(썬_콜)", 0), listOf("체인 라이트닝 VI", listOf(335, 335, 335, 335, 335, 335, 335, 335, 335, 335), 780, 0)),
		Pair(Pair("아크메이지(불_독)", 0), listOf("플레임 스윕 VI", listOf(355, 355, 355, 355, 355, 355, 355), 780, 0)),
		Pair(Pair("비숍", 0), listOf("엔젤레이 VI", listOf(355, 355, 355, 355, 355, 355, 355, 355, 355, 355, 355, 355, 355, 355), 810, 0)),
		Pair(Pair("나이트로드", 0), listOf("쿼드러플 스로우 VI", listOf(990, 990, 990, 990), 780, 0)),
		Pair(Pair("섀도어", 0), listOf("암살 VI", listOf(690, 690, 690, 690, 690, 690), 690, 0)),
		Pair(Pair("듀얼블레이드", 0), listOf("팬텀 블로우 VI", listOf(530, 530, 530, 530, 530, 530), 720, 0)),
		Pair(Pair("바이퍼", 0), listOf("피스트 인레이지 VI", listOf(455, 455, 455, 455, 455, 455, 455, 455, 455, 455), 780, 0)),
		Pair(Pair("캡틴", 0), listOf("래피드 파이어 VI", listOf(555), 120, 0)),
		Pair(Pair("캐논슈터", 0), listOf("캐논 버스터 VI", listOf(1240, 1240, 1240, 1240), 810, 0)),
		Pair(Pair("블래스터", 0), listOf("매그넘 펀치 VI", listOf(985, 985, 985), 1170, 0)),
		Pair(Pair("와일드헌터", 0), listOf("와일드 발칸 VI", listOf(530), 60, 0)),
		Pair(Pair("배틀메이지", 0), listOf("데스 VI", listOf(930, 930, 930, 930, 930, 930, 930, 930, 930, 930, 930, 930), 2070, 0)),
		Pair(Pair("제논", 0), listOf("퍼지롭 매스커레이드 VI : 저격", listOf(510, 510, 510, 510, 510, 510, 510), 1290, 0)),
		Pair(Pair("메카닉", 0), listOf("매시브 파이어 : IRON-B VI", listOf(570, 570, 570, 570, 570, 570, 390, 390, 390), 1080, 0)),
		Pair(Pair("아란", 0), listOf("비욘더 VI", listOf(540, 540, 540, 540, 540), 960, 0)),
		Pair(Pair("메르세데스", 0), listOf("이슈타르의 링 VI", listOf(525, 525), 480, 0)),
		Pair(Pair("루미너스", 0), listOf("앱솔루트 킬 VI", listOf(695, 695, 695, 695, 695, 695, 695), 960, 0)),
		Pair(Pair("에반", 0), listOf("서클 오브 마나 VI", listOf(555, 555, 555, 555, 625, 625, 625, 625), 1080, 0)),
		Pair(Pair("팬텀", 0), listOf("템페스트 오브 카드 VI", listOf(840, 840, 840, 840), 720, 0)),
		Pair(Pair("은월", 0), listOf("귀참 VI", listOf(475, 475, 475, 475, 475, 475, 475, 475, 475, 475, 475, 475), 1350, 0)),
	)

	@SuppressLint("SetTextI18n", "DiscouragedApi")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		db = Database.getInstance(applicationContext)!!  // 데이터베이스 생성
		binding = ActivityDamageTestBinding.inflate(layoutInflater)
		val damageTextList = listOf(binding.damageText1, binding.damageText2, binding.damageText3, binding.damageText4, binding.damageText5,
			binding.damageText6, binding.damageText7, binding.damageText8, binding.damageText9, binding.damageText10,
			binding.damageText11, binding.damageText12, binding.damageText13, binding.damageText14, binding.damageText15)
		characterId = intent.getIntExtra("characterId", 0)
		bossId = intent.getIntExtra("bossId", 0)
		setContentView(binding.root)
		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				itemsDao = db.itemsDao()
				itemTableDao = db.itemTableDao()
				characterData = db.charactersDao().getByCharacterId(characterId)[0]
				cubeTables = db.cubeTableDao().getAll()
			}.join()
		}

		val boss = BossInfo.getBossByBossId(bossId)
		bossMaxHP = boss.maxHP; bossHP = bossMaxHP
		binding.totalDamage.text = boss.info
		binding.bossImage.setImageResource(resources.getIdentifier(
			boss.englishName, "drawable", packageName))
		val listener: RequestListener<GifDrawable> by lazy {
			object : RequestListener<GifDrawable> {
				override fun onLoadFailed(
					e: GlideException?, model: Any,
					target: com.bumptech.glide.request.target.Target<GifDrawable>?,
					isFirstResource: Boolean
				): Boolean {
					return false
				}
				override fun onResourceReady(
					resource: GifDrawable,
					model: Any,
					target: com.bumptech.glide.request.target.Target<GifDrawable>?,
					dataSource: DataSource,
					isFirstResource: Boolean
				): Boolean {
					val callback = object : Animatable2Compat.AnimationCallback() {
						override fun onAnimationStart(drawable: Drawable?) {
							super.onAnimationStart(drawable)
						}

						override fun onAnimationEnd(drawable: Drawable?) {
							for(damageText in damageTextList)
								damageText.visibility = View.INVISIBLE
							binding.skillUseButton.isEnabled = true
							super.onAnimationEnd(drawable)
						}
					}
					resource.registerAnimationCallback(callback)
					return false
				}
			}
		}
		val character = Character(characterData, itemsDao, itemTableDao, cubeTables, characterId)
		var damageList = emptyList<Long>()
		var dj = when (AppData.characterCodeList[character.jobName]?.second) {
			"soulmaster", "striker", "nightwalker", "flamewizard", "adele", "windbreaker" -> Glide.with(this).asGif()
				.load(resources.getIdentifier("skill_${AppData.characterCodeList[character.jobName]?.second}_0", "raw", packageName)).listener(listener).skipMemoryCache(true)
			else -> Glide.with(this).asGif()
				.load(resources.getIdentifier("skill_base", "raw", packageName)).listener(listener).skipMemoryCache(true)
		}
		val deciFormat = DecimalFormat("#,###")
		binding.skillImage.visibility = View.INVISIBLE

		class ListViewHolder(val binding: DamageLayoutBinding): RecyclerView.ViewHolder(binding.root)
		class ListAdapterHorizontal(val index: Int):
			RecyclerView.Adapter<RecyclerView.ViewHolder>(){
			override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
				return ListViewHolder(DamageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
			}
			@SuppressLint("DiscouragedApi")
			override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
				val bindingViewHolder = (holder as ListViewHolder).binding
				bindingViewHolder.listItemImg.setImageResource(
					resources.getIdentifier("damage_adele${damageList[index].toString()[position]}", "drawable", packageName))
			}
			override fun getItemCount(): Int {
 				return damageList[index].toString().length
			}
		}

		for(i in damageTextList.indices) {
			val layoutManager =  LinearLayoutManager(this@DamageTestActivity)
			layoutManager.orientation = LinearLayoutManager.HORIZONTAL
			damageTextList[i].layoutManager = layoutManager
			//damageTextList[i].adapter = ListAdapterHorizontal(i)
		}
		for(damageText in damageTextList)
			damageText.visibility = View.INVISIBLE

		var totalDamage = 0L
		var time = System.currentTimeMillis()
		var killBoss = false
		
		binding.skillUseButton.setOnClickListener {
			if (binding.skillUseButton.text == "스킬 사용") {
				dj = Glide.with(this).asGif()
					.load(resources.getIdentifier("skill_${AppData.characterCodeList[character.jobName]?.second}_0", "raw", packageName))
					.listener(listener)

				dj = when (AppData.characterCodeList[character.jobName]?.second) {
					"flamewizard","windbreaker", "bowmaster", "captain" -> dj.skipMemoryCache(false)
					else -> dj.skipMemoryCache(true)
				}
				binding.skillUseButton.text = "사용 중지"
				binding.skillImage.visibility = View.VISIBLE
				val skill = skillMap[Pair(character.jobName, 0)]
				val skillDelay = skill?.get(2) as Int
				val skillCool = skill[3] as Int
				val period = maxOf(skillDelay, skillCool).toLong()

				timer(period = period) {
					if(binding.skillUseButton.text == "스킬 사용") {
						CoroutineScope(Dispatchers.Main).launch {
							dj.into(binding.skillImage)
							for (damageText in damageTextList)
								damageText.visibility = View.INVISIBLE
							binding.skillImage.visibility = View.INVISIBLE
							binding.skillUseButton.isEnabled = true
						}
						cancel()
					}
					else {
						runBlocking {
							CoroutineScope(Dispatchers.Main).launch {
								dj.into(binding.skillImage)// 스킬 GIF 애니메이션 넣기
							}.join()
						}
						damageList = character.skillDamage(skill[1] as List<Int>, boss.armor)
						totalDamage = 0
						for(damage in damageList)
							totalDamage += damage
						bossHP -= totalDamage
						if(bossHP <= 0) {
							bossHP = 0
							if(!killBoss) {
								killBoss = true
								runBlocking {
									CoroutineScope(Dispatchers.IO).launch {
										for(dropItem in boss.dropItems) {
											itemTableDao.updateCanCreate(dropItem)
										}
									}.join()
								}
							}
						}
						CoroutineScope(Dispatchers.Main).launch {
							binding.totalDamage.text = "${boss.info}\n${boss.koreanName}\n${deciFormat.format(bossHP)}(${String.format("%.2f", bossHP.toDouble()/bossMaxHP.toDouble()*100)}%)\n${deciFormat.format(totalDamage)}"
						}
						var i = 0
						timer(period = 25, initialDelay = maxOf(period/2 - 25 * damageList.size / 2, 0)) {
							if(i == 0) {
								runBlocking {
									CoroutineScope(Dispatchers.Main).launch {
										for (damageText in damageTextList)
											damageText.visibility = View.INVISIBLE
									}.join()
								}
							}
							if(i < damageList.size && binding.skillUseButton.text == "사용 중지") {
								runBlocking {
									CoroutineScope(Dispatchers.Main).launch {
										damageTextList[7 - damageList.size/2 + i].visibility = View.VISIBLE
										damageTextList[7 - damageList.size/2 + i].adapter = ListAdapterHorizontal(i)
									}.join()
								}
								i++
							} else {
								cancel()
							}
						}
					}
				}
			} else {
				binding.skillUseButton.text = "스킬 사용"
				binding.skillUseButton.isEnabled = false
			}
		}
	}
}