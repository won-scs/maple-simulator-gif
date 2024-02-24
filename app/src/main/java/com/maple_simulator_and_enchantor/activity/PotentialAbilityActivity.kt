package com.maple_simulator_and_enchantor.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.maple_simulator_and_enchantor.AppData
import com.maple_simulator_and_enchantor.AppData.Companion.abilityOptionTypeList
import com.maple_simulator_and_enchantor.Item
import com.maple_simulator_and_enchantor.R
import com.maple_simulator_and_enchantor.database.Database
import com.maple_simulator_and_enchantor.database.ItemsDao
import com.maple_simulator_and_enchantor.databinding.ActivityPotentialAbilityBinding
import com.maple_simulator_and_enchantor.databinding.BeforeAfterChooseHoriBinding
import com.maple_simulator_and_enchantor.databinding.ListItemHoriBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

class PotentialAbilityActivity : AppCompatActivity() {
	lateinit var db: Database
	lateinit var binding: ActivityPotentialAbilityBinding
	lateinit var itItem: Item
	private val cubeList = ArrayList<Int>()
	val useCubeNameList = listOf("레드 큐브", "블랙 큐브", "에디셔널 큐브", "화이트 에디셔널 큐브")
	private val useCubeNameMap = mapOf(Pair("레드 큐브", 0), Pair("블랙 큐브", 1), Pair("에디셔널 큐브", 2), Pair("화이트 에디셔널 큐브", 3))
	var useCubeName: String = "레드 큐브"
	private val visibleCheckList = ArrayList<Boolean>()
	private lateinit var aboutInventoryIntent: Intent
	private lateinit var itemsDao: ItemsDao
	private lateinit var beforeCubeUpAbility: MutableList<Int>
	private lateinit var beforeCubeDownAbility: MutableList<Int>
	private var afterCubeUpAbility = List<Int>(100){0}.toMutableList()
	private var afterCubeDownAbility = List<Int>(100){0}.toMutableList()
	private var cubeTextArrayList = ArrayList<SpannableStringBuilder>()
	private var wantedOptionType = ""
	private var wantedOptionValue = ""
	private lateinit var wantedOptionValueList: Array<String>
	private var isFirst = false
	private var miracleBtn = true
	private val abilityOptionTypeList = AppData.abilityOptionTypeList
	var isResumedActivity = false
	var isHomeButtonPressed = false
	private val wOptTxtMap1 = emptyMap<String, List<String>>().toMutableMap()
	private val wOptTxtMap2 = emptyMap<String, List<String>>().toMutableMap()
	private var isPlayAnimation = false

	private fun setWantedOptionTextMap() {
		wOptTxtMap1["STR 3줄"] = listOf("STR%", "캐릭터 기준 9레벨 당 STR", "올스탯%")
		wOptTxtMap1["DEX 3줄"] = listOf("DEX%", "캐릭터 기준 9레벨 당 DEX", "올스탯%")
		wOptTxtMap1["INT 3줄"] = listOf("INT%", "캐릭터 기준 9레벨 당 INT", "올스탯%")
		wOptTxtMap1["LUK 3줄"] = listOf("LUK%", "캐릭터 기준 9레벨 당 LUK", "올스탯%")
		wOptTxtMap1["올스탯 3줄"] = listOf("올스탯%")
		wOptTxtMap1["올스탯 2줄"] = listOf("올스탯%")
		wOptTxtMap1["공격력% 3줄"] = listOf("공격력%")
		wOptTxtMap1["마력% 3줄"] = listOf("마력%")
		wOptTxtMap1["크크크"] = listOf("크리티컬 데미지%")
		wOptTxtMap1["쿨쿨쿨"] = listOf("모든 스킬의 재사용 대기시간")
		wOptTxtMap1["보보공"] = listOf("보스 몬스터 공격 시 데미지%")
		wOptTxtMap1["보보마"] = listOf("보스 몬스터 공격 시 데미지%")
		wOptTxtMap1["보보방"] = listOf("보스 몬스터 공격 시 데미지%")
		wOptTxtMap1["보공공"] = listOf("공격력%")
		wOptTxtMap1["보마마"] = listOf("마력%")
		wOptTxtMap1["방공공"] = listOf("공격력%")
		wOptTxtMap1["방마마"] = listOf("마력%")
		wOptTxtMap1["크힘힘"] = listOf("STR%", "캐릭터 기준 9레벨 당 STR", "올스탯%")
		wOptTxtMap1["크덱덱"] = listOf("DEX%", "캐릭터 기준 9레벨 당 DEX", "올스탯%")
		wOptTxtMap1["크인인"] = listOf("INT%", "캐릭터 기준 9레벨 당 INT", "올스탯%")
		wOptTxtMap1["크럭럭"] = listOf("LUK%", "캐릭터 기준 9레벨 당 LUK", "올스탯%")
		wOptTxtMap1["크크힘"] = listOf("크리티컬 데미지%")
		wOptTxtMap1["크크덱"] = listOf("크리티컬 데미지%")
		wOptTxtMap1["크크인"] = listOf("크리티컬 데미지%")
		wOptTxtMap1["크크럭"] = listOf("크리티컬 데미지%")
		wOptTxtMap1["크크"] = listOf("크리티컬 데미지%")
		wOptTxtMap1["쿨힘힘"] = listOf("STR%", "캐릭터 기준 9레벨 당 STR", "올스탯%")
		wOptTxtMap1["쿨덱덱"] = listOf("DEX%", "캐릭터 기준 9레벨 당 DEX", "올스탯%")
		wOptTxtMap1["쿨인인"] = listOf("INT%", "캐릭터 기준 9레벨 당 INT", "올스탯%")
		wOptTxtMap1["쿨럭럭"] = listOf("LUK%", "캐릭터 기준 9레벨 당 LUK", "올스탯%")
		wOptTxtMap1["쿨쿨힘"] = listOf("모든 스킬의 재사용 대기시간")
		wOptTxtMap1["쿨쿨덱"] = listOf("모든 스킬의 재사용 대기시간")
		wOptTxtMap1["쿨쿨인"] = listOf("모든 스킬의 재사용 대기시간")
		wOptTxtMap1["쿨쿨럭"] = listOf("모든 스킬의 재사용 대기시간")
		wOptTxtMap1["쿨쿨"] = listOf("모든 스킬의 재사용 대기시간")

		wOptTxtMap2["STR 3줄"] = listOf("STR%", "캐릭터 기준 9레벨 당 STR", "올스탯%")
		wOptTxtMap2["DEX 3줄"] = listOf("DEX%", "캐릭터 기준 9레벨 당 DEX", "올스탯%")
		wOptTxtMap2["INT 3줄"] = listOf("INT%", "캐릭터 기준 9레벨 당 INT", "올스탯%")
		wOptTxtMap2["LUK 3줄"] = listOf("LUK%", "캐릭터 기준 9레벨 당 LUK", "올스탯%")
		wOptTxtMap2["올스탯 3줄"] = listOf("올스탯%")
		wOptTxtMap2["올스탯 2줄"] = AppData.abilityOptionTypeList
		wOptTxtMap2["공격력% 3줄"] = listOf("공격력%")
		wOptTxtMap2["마력% 3줄"] = listOf("마력%")
		wOptTxtMap2["크크크"] = listOf("크리티컬 데미지%")
		wOptTxtMap2["쿨쿨쿨"] = listOf("모든 스킬의 재사용 대기시간")
		wOptTxtMap2["보보공"] = listOf("공격력%")
		wOptTxtMap2["보보마"] = listOf("마력%")
		wOptTxtMap2["보보방"] = listOf("몬스터 방어율 무시%")
		wOptTxtMap2["보공공"] = listOf("보스 몬스터 공격 시 데미지%")
		wOptTxtMap2["보마마"] = listOf("보스 몬스터 공격 시 데미지%")
		wOptTxtMap2["방공공"] = listOf("몬스터 방어율 무시%")
		wOptTxtMap2["방마마"] = listOf("몬스터 방어율 무시%")
		wOptTxtMap2["크힘힘"] = listOf("크리티컬 데미지%")
		wOptTxtMap2["크덱덱"] = listOf("크리티컬 데미지%")
		wOptTxtMap2["크인인"] = listOf("크리티컬 데미지%")
		wOptTxtMap2["크럭럭"] = listOf("크리티컬 데미지%")
		wOptTxtMap2["크크힘"] = listOf("STR%", "캐릭터 기준 9레벨 당 STR", "올스탯%")
		wOptTxtMap2["크크덱"] = listOf("DEX%", "캐릭터 기준 9레벨 당 DEX", "올스탯%")
		wOptTxtMap2["크크인"] = listOf("INT%", "캐릭터 기준 9레벨 당 INT", "올스탯%")
		wOptTxtMap2["크크럭"] = listOf("LUK%", "캐릭터 기준 9레벨 당 LUK", "올스탯%")
		wOptTxtMap2["크크"] = AppData.abilityOptionTypeList
		wOptTxtMap2["쿨힘힘"] = listOf("모든 스킬의 재사용 대기시간")
		wOptTxtMap2["쿨덱덱"] = listOf("모든 스킬의 재사용 대기시간")
		wOptTxtMap2["쿨인인"] = listOf("모든 스킬의 재사용 대기시간")
		wOptTxtMap2["쿨럭럭"] = listOf("모든 스킬의 재사용 대기시간")
		wOptTxtMap2["쿨쿨힘"] = listOf("STR%", "캐릭터 기준 9레벨 당 STR", "올스탯%")
		wOptTxtMap2["쿨쿨덱"] = listOf("DEX%", "캐릭터 기준 9레벨 당 DEX", "올스탯%")
		wOptTxtMap2["쿨쿨인"] = listOf("INT%", "캐릭터 기준 9레벨 당 INT", "올스탯%")
		wOptTxtMap2["쿨쿨럭"] = listOf("LUK%", "캐릭터 기준 9레벨 당 LUK", "올스탯%")
		wOptTxtMap2["쿨쿨"] = AppData.abilityOptionTypeList
	}

	@SuppressLint("DiscouragedApi")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityPotentialAbilityBinding.inflate(layoutInflater)
		aboutInventoryIntent = intent
		db = Database.getInstance(applicationContext)!!  // 데이터베이스 생성
		setContentView(binding.root)

		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				itemsDao = db.itemsDao()
				val itemData = itemsDao.getById(aboutInventoryIntent.getIntExtra("itemId", 0))[0]
				val cubeTables = db.cubeTableDao().getAll()
				itItem = Item(itemsDao, itemData, cubeTables)
			}.join()
		}

		setWantedOptionTextMap()

		binding.AbilityEquipImg.setImageResource(resources.getIdentifier("id${itItem.itemCode}", "drawable", packageName))
		binding.AbilityEquipNameText.text = itItem.name

		beforeCubeUpAbility = itItem.upAbility
		beforeCubeDownAbility = itItem.downAbility
		afterCubeUpAbility = itItem.upAbility
		afterCubeDownAbility = itItem.downAbility

		binding.cubeUseBtn.isEnabled = false

		val fadeInOutAnimation_inCube = AnimationUtils.loadAnimation(applicationContext,
			R.anim.fade_inandout
		)
		fadeInOutAnimation_inCube.setAnimationListener (
			object : AnimationListener {
				override fun onAnimationStart(animation: Animation) {
					binding.cubeUseBtn.isEnabled = false
					isPlayAnimation = true
				}
				override fun onAnimationEnd(animation: Animation) {
					binding.cubeUseBtn.isEnabled =
						(afterCubeUpAbility[0] <= beforeCubeUpAbility[0] &&
							afterCubeDownAbility[0] <= beforeCubeDownAbility[0])
					isPlayAnimation = false
				}
				override fun onAnimationRepeat(animation: Animation) {
				}
			}
		)

		class ListViewHolder(val binding: ListItemHoriBinding): RecyclerView.ViewHolder(binding.root)
		class ListAdapterHorizontal(val datas: ArrayList<Int>, val visibleCheck: ArrayList<Boolean>):
			RecyclerView.Adapter<RecyclerView.ViewHolder>(){
			override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
				return ListViewHolder(ListItemHoriBinding.inflate(LayoutInflater.from(parent.context), parent, false))
			}
			override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
				val bindingViewHolder = (holder as ListViewHolder).binding
				if(visibleCheck[position]) bindingViewHolder.checkBtn.visibility = View.VISIBLE
				else bindingViewHolder.checkBtn.visibility = View.INVISIBLE
				bindingViewHolder.listItemImg.setImageResource(datas[position])
				bindingViewHolder.listItemImg.setOnClickListener {
					if (binding.cubeUseBtn.text == "사용하기" && !isPlayAnimation) {
						for (i in 0 until visibleCheckList.size) {
							if (i != position)
								visibleCheckList[i] = false
						}
						visibleCheckList[position] = !visibleCheckList[position]
						if (visibleCheckList[position]) {
							useCubeName = useCubeNameList[position]
							updateWantedOptionAdapter()
							binding.cubeUseBtn.isEnabled = true
						} else {
							useCubeName = ""
							binding.cubeUseBtn.isEnabled = false
						}
						binding.cubeBtnList.adapter = ListAdapterHorizontal(cubeList, visibleCheckList)
					}
				}
			}
			override fun getItemCount(): Int {
				return datas.size
			}
		}

		cubeList.add(R.drawable.red_cube)
		visibleCheckList.add(false)
		cubeList.add(R.drawable.black_cube)
		visibleCheckList.add(false)
		cubeList.add(R.drawable.additional_cube)
		visibleCheckList.add(false)
		cubeList.add(R.drawable.white_additional_cube)
		visibleCheckList.add(false)
		val layoutManager =  LinearLayoutManager(this)
		layoutManager.orientation = LinearLayoutManager.HORIZONTAL
		binding.cubeBtnList.layoutManager = layoutManager
		binding.cubeBtnList.adapter = ListAdapterHorizontal(cubeList, visibleCheckList)

		class ListViewHolder2(val binding: BeforeAfterChooseHoriBinding): RecyclerView.ViewHolder(binding.root)
		class ListAdapterHorizontal2(val datas: ArrayList<SpannableStringBuilder>):
			RecyclerView.Adapter<RecyclerView.ViewHolder>(){

			fun resizeItems() {
				when (useCubeName) {
					"레드 큐브", "에디셔널 큐브" -> {
						if(datas.size == 2) datas.removeAt(1)
						notifyItemRemoved(1)
					}
					"블랙 큐브", "화이트 에디셔널 큐브" -> {
						if(datas.size == 1) datas.add(datas[0])
						notifyItemInserted(1)
					}
				}
			}

			fun editItem() {
				datas[datas.size-1] = cubeOptionText(afterCubeUpAbility, afterCubeDownAbility)
				notifyItemChanged(datas.size-1)
			}

			override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
				return ListViewHolder2(BeforeAfterChooseHoriBinding.inflate(LayoutInflater.from(parent.context), parent, false))
			}

			override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
				val bindingViewHolder2 = (holder as ListViewHolder2).binding
				bindingViewHolder2.optionChoiceBtn.setOnClickListener {
					binding.cubeUseBtn.isEnabled = true
					if(position == 0) {
						itItem.upAbility = beforeCubeUpAbility
						itItem.downAbility = beforeCubeDownAbility
						afterCubeUpAbility = beforeCubeUpAbility
						afterCubeDownAbility = beforeCubeDownAbility
					} else if(position == 1) {
						itItem.upAbility = afterCubeUpAbility
						itItem.downAbility = afterCubeDownAbility
						runBlocking {
							CoroutineScope(Dispatchers.IO).launch {
								itemsDao.updateUpAbility(afterCubeUpAbility, itItem.itemId)
								itemsDao.updateDownAbility(afterCubeDownAbility, itItem.itemId)
							}.join()
						}
					}
					if(datas.size == 2)
						datas.removeAt(1)
					binding.cubeInfoTextList.layoutManager = GridLayoutManager(this@PotentialAbilityActivity, cubeTextArrayList.size)
					editItem()
				}
				bindingViewHolder2.optionInfoText.text = datas[position]

				bindingViewHolder2.optionChoiceBtn.visibility = if(datas.size == 2) View.VISIBLE else View.INVISIBLE
				bindingViewHolder2.optionChoiceBtn.isEnabled = binding.cubeUseBtn.text == "사용하기"
				if(position == datas.size - 1) {
//					Log.d("1", "${binding.cubeUseBtn.text} ${isFirst}")
					if(binding.cubeUseBtn.text == "사용하기" && isFirst) {
						bindingViewHolder2.optionInfoText.startAnimation(fadeInOutAnimation_inCube)
						isFirst = false
					}
				}
			}
			override fun getItemCount(): Int {
				return datas.size
			}
		}

		cubeTextArrayList = ArrayList(listOf(cubeOptionText(beforeCubeUpAbility, beforeCubeDownAbility)))
		val mAdapter = ListAdapterHorizontal2(cubeTextArrayList)
		binding.cubeInfoTextList.itemAnimator = null
		binding.cubeInfoTextList.layoutManager = GridLayoutManager(this@PotentialAbilityActivity, cubeTextArrayList.size)
		binding.cubeInfoTextList.adapter = mAdapter

		wantedOptionValueList = resources.getStringArray(R.array.wantedOptionValueList30_33_36)
		binding.wantedOptionValueSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, wantedOptionValueList)
		binding.wantedOptionValueSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				wantedOptionValue = wantedOptionValueList[position]
			}
			override fun onNothingSelected(parent: AdapterView<*>?) {
			}
		}

		val wantedOptionTypeList = resources.getStringArray(R.array.wantedOptionTypeList)
		binding.wantedOptionTypeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, wantedOptionTypeList)
		binding.wantedOptionTypeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				wantedOptionType = wantedOptionTypeList[position]
				updateWantedOptionAdapter()
			}
			override fun onNothingSelected(parent: AdapterView<*>?) {
			}
		}

		fun cubeOneUse() {
			itItem.useCash[useCubeNameMap[useCubeName]!!+1]++
			runBlocking {
				CoroutineScope(Dispatchers.IO).launch {
					itemsDao.updateUseInfo(itItem.useMeso, itItem.useCash, itItem.destroyNum, itItem.itemId)
				}.join()
			}
			beforeCubeUpAbility = itItem.upAbility
			beforeCubeDownAbility = itItem.downAbility
			itItem.gradeUp(useCubeNameMap[useCubeName]!!+1, miracleBtn)
			itItem.optionChange(useCubeNameMap[useCubeName]!!+1)
			afterCubeUpAbility = itItem.upAbility
			afterCubeDownAbility = itItem.downAbility
			when (useCubeName) {
				"레드 큐브", "에디셔널 큐브" -> {
					runBlocking {
						CoroutineScope(Dispatchers.IO).launch {
							itemsDao.updateUpAbility(itItem.upAbility, itItem.itemId)
							itemsDao.updateDownAbility(itItem.downAbility, itItem.itemId)
						}.join()
					}
				}
				"블랙 큐브", "화이트 에디셔널 큐브" -> {
					itItem.upAbility = beforeCubeUpAbility
					itItem.downAbility = beforeCubeDownAbility
				}
			}
		}

		// startOrEnd true: start, false: end
		@SuppressLint("NotifyDataSetChanged")
		fun manageAuto(startOrEnd: String) {
			binding.autoOptionChangeCheckBox.isEnabled = startOrEnd != "start"
			binding.cubeUseBtn.text = if(startOrEnd == "start") "STOP" else "사용하기"
			mAdapter.notifyDataSetChanged()
		}

		fun setCubeInfoTextList() {
			if(binding.cubeUseBtn.text == "사용하기") {
				mAdapter.resizeItems()
				binding.cubeInfoTextList.layoutManager =
					GridLayoutManager(this@PotentialAbilityActivity, mAdapter.itemCount)
			}
		}
		val autoCubeTimer = Timer()

		binding.cubeUseBtn.setOnClickListener {
			setCubeInfoTextList() // 옵션 선택 가능 여부에 따른 리스트뷰 갱신
			isFirst = true
			if(binding.cubeUseBtn.text == "STOP"){
				binding.cubeUseBtn.text = "일시정지"
			} else if(binding.autoOptionChangeCheckBox.isChecked) {
				manageAuto("start")
				val autoCubeTimerTask = object: TimerTask() {
					override fun run() {
						// 홈버튼을 누르지 않고 액티비티를 종료했을 경우
						if(!isResumedActivity && !isHomeButtonPressed) { cancel() }
						cubeOneUse()
						runBlocking {
							CoroutineScope(Dispatchers.Main).launch() {
								mAdapter.editItem()
							}.join()
						}
						if(binding.cubeUseBtn.text != "STOP" ||
							(useCubeName == "블랙 큐브" && afterCubeUpAbility[0] -  beforeCubeUpAbility[0] > 0) ||
							(useCubeName == "화이트 에디셔널 큐브" && afterCubeDownAbility[0] -  beforeCubeDownAbility[0] > 0)) {
							runOnUiThread {
								manageAuto("end")
							}
							cancel()
						}


						val afterAbility = when(useCubeName) {
							"레드 큐브", "블랙 큐브" -> afterCubeUpAbility
							else -> afterCubeDownAbility
						}
						// 원하는 옵션이 몇개 떴는지
						var getWOptNum1 = 0; var getWOptNum2 = 0
						// 원하는 옵션 후보 리스트
						val wOptTxtList1 = wOptTxtMap1[wantedOptionType]; val wOptTxtList2 = wOptTxtMap2[wantedOptionType]
						if (wOptTxtList1 != null && wOptTxtList2 != null) {
							for(i in 2..4) {
								if (wOptTxtList1.contains(abilityOptionTypeList[afterAbility[i] % 100])) getWOptNum1++
								if (wOptTxtList2.contains(abilityOptionTypeList[afterAbility[i] % 100])) getWOptNum2++
							}
							when(wantedOptionType) {
								"STR 3줄", "DEX 3줄", "INT 3줄", "LUK 3줄", "올스탯 3줄", "공격력% 3줄", "마력% 3줄", "크크크", "쿨쿨쿨" -> {
									if(getWOptNum1 + getWOptNum2 != 6) {
										getWOptNum1 = 0; getWOptNum2 = 0
									}
								}
							}
							if(getWOptNum1 >= 2 && getWOptNum2 >= 1) {
								var getWOptSuc = true
								if(useCubeName == "레드 큐브" || useCubeName == "블랙 큐브") {
									when (wantedOptionType) {
										"STR 3줄", "DEX 3줄", "INT 3줄", "LUK 3줄", "공격력% 3줄", "마력% 3줄" -> {
											var wOptVal = 0
											for(i in 2..4) wOptVal += afterAbility[i]/100
											try {
												if(wOptVal < wantedOptionValue.split("%")[0].toInt())
													getWOptSuc = false
											} catch (_: Exception) { }
										}
									}
								} else {
									when (wantedOptionType) {
										"공격력% 3줄", "마력% 3줄" -> {
											var wOptVal = 0
											for(i in 2..4) wOptVal += afterAbility[i]/100
											try {
												if(wOptVal < wantedOptionValue.split("%")[0].toInt())
													getWOptSuc = false
											} catch (_: Exception) { }
										}
									}
								}
								if(getWOptSuc) {
									runOnUiThread {
										manageAuto("end")
									}
									cancel()
								}
							}
						}
					}
				}
				autoCubeTimer.schedule(autoCubeTimerTask, 100, 25)
			} else if(!binding.autoOptionChangeCheckBox.isChecked) {
				cubeOneUse()
				mAdapter.editItem()
			}
		}
	}

	override fun onResume() {
		super.onResume()
		binding.autoOptionChangeCheckBox.isEnabled = true
		isResumedActivity = true
		isHomeButtonPressed = false
	}

	override fun onUserLeaveHint() {
		super.onUserLeaveHint()
		isHomeButtonPressed = true
	}

	override fun onPause() {
		super.onPause()
		isResumedActivity = false
	}

	override fun onStop() {
		super.onStop()
		isResumedActivity = false
	}

	private fun cubeOptionText(upAbility: MutableList<Int>, downAbility: MutableList<Int>): SpannableStringBuilder {
		val builder: SpannableStringBuilder = SpannableStringBuilder("")
		builder.append(abilityGrade(upAbility[0], 0))
		for(i in 2..4) builder.append(AppData.potentialAbilityTextHelper(upAbility[i]))
		builder.append("\n")
		builder.append(abilityGrade(downAbility[0], 1))
		for(i in 2..4) builder.append(AppData.potentialAbilityTextHelper(downAbility[i]))
		return builder
	}

	@SuppressLint("ResourceAsColor")
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

	fun updateWantedOptionAdapter() {
		wantedOptionValueList = when(useCubeName) {
			"레드 큐브", "블랙 큐브" -> {
				when(wantedOptionType) {
					"STR 3줄", "DEX 3줄", "INT 3줄", "LUK 3줄", "공격력% 3줄", "마력% 3줄" ->
						resources.getStringArray(R.array.wantedOptionValueList30_33_36)
					else -> resources.getStringArray(R.array.wantedOptionValueListDefault)
				}
			} else -> {
				when(wantedOptionType) {
					"공격력% 3줄", "마력% 3줄" ->
						resources.getStringArray(R.array.wantedOptionValueList30_33_36)
					else -> resources.getStringArray(R.array.wantedOptionValueListDefault)
				}
			}
		}
		binding.wantedOptionValueSpinner.adapter = ArrayAdapter(this@PotentialAbilityActivity,
			android.R.layout.simple_spinner_dropdown_item, wantedOptionValueList)
	}
}