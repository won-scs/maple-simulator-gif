package com.maple_simulator_and_enchantor.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.maple_simulator_and_enchantor.Item
import com.maple_simulator_and_enchantor.R
import com.maple_simulator_and_enchantor.database.Database
import com.maple_simulator_and_enchantor.database.ItemsDao
import com.maple_simulator_and_enchantor.databinding.ActivityExtraOptionBinding
import com.maple_simulator_and_enchantor.databinding.BeforeAfterChooseHoriBinding
import com.maple_simulator_and_enchantor.databinding.ListItemHoriBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.timer
import kotlin.math.ceil

class ExtraOptionActivity : AppCompatActivity() {
	lateinit var db: Database
	lateinit var binding: ActivityExtraOptionBinding
	lateinit var itItem: Item
	var selectedFlameId: Int = 0
	private lateinit var aboutInventoryIntent: Intent
	private var statAtkBefore = 0.0
	private lateinit var itemsDao: ItemsDao
	private val flameList = ArrayList<Int>()
	private val visibleCheckList = ArrayList<Boolean>()
	private lateinit var beforeExtraStat: MutableList<Int>
	private var afterExtraStat = List<Int>(100){0}.toMutableList()
	private var imsiArrayList = ArrayList<Int>(listOf(1))
	var isFirst = true
	var isResumedActivity = false
	var isHomeButtonPressed = false

	@SuppressLint("DiscouragedApi")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityExtraOptionBinding.inflate(layoutInflater)
		setContentView(binding.root)
		aboutInventoryIntent = intent

		db = Database.getInstance(applicationContext)!!  // 데이터베이스 생성
		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				itemsDao = db.itemsDao()
				val itemData = itemsDao.getById(aboutInventoryIntent.getIntExtra("itemId", 0))[0]
				val cubeTables =  db.cubeTableDao().getAll()
				itItem = Item(itemsDao, itemData, cubeTables)
			}.join()
		}

		binding.extraOptionEquipImg.setImageResource(resources.getIdentifier(
			"id${itItem.itemCode}", "drawable", packageName))
		binding.extraOptionEquipNameText.text = itItem.name

		beforeExtraStat = itItem.extraStat
		afterExtraStat = itItem.extraStat

		binding.extraOptionUseBtn.isEnabled = false

		var selectedWantedOption = -1
		var selectedWantedValue = -1

		val wantedOptionList = resources.getStringArray(R.array.wantedExtraOptionList)
		var wantedValueList = resources.getStringArray(R.array.wantedExtraValueList)
		binding.wantedOptionSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, wantedOptionList)
		binding.wantedOptionSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				selectedWantedOption = position
				wantedValueList = when(selectedWantedOption) {
					0, 1, 2, 3 -> {
						resources.getStringArray(R.array.wantedExtraValueList)
					}
					else -> {
						resources.getStringArray(R.array.wantedExtraValueList2)
					}
				}
				binding.wantedValueSpinner.adapter = ArrayAdapter(this@ExtraOptionActivity, android.R.layout.simple_spinner_dropdown_item, wantedValueList)
				selectedWantedValue = 0
			}
			override fun onNothingSelected(parent: AdapterView<*>?) {
			}
		}
		binding.wantedValueSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, wantedValueList)
		binding.wantedValueSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				selectedWantedValue = position
			}
			override fun onNothingSelected(parent: AdapterView<*>?) {
			}
		}

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
					if (binding.extraOptionUseBtn.text != "STOP") {
						for (i in 0 until visibleCheckList.size) {
							if (i != position)
								visibleCheckList[i] = false
						}
						visibleCheckList[position] = !visibleCheckList[position]
						if (visibleCheckList[position]) {
							selectedFlameId = position + 1
							binding.extraOptionUseBtn.isEnabled = true
						} else {
							selectedFlameId = 0
							binding.extraOptionUseBtn.isEnabled = false
						}
						binding.flameBtnList.adapter = ListAdapterHorizontal(flameList, visibleCheckList)
					}
				}
			}
			override fun getItemCount(): Int {
				return datas.size
			}
		}

		flameList.add(R.drawable.eternal_flame_resurrection)
		visibleCheckList.add(false)
		flameList.add(R.drawable.strong_flame_resurrection)
		visibleCheckList.add(false)
		flameList.add(R.drawable.black_eternal_flame_resurrection)
		visibleCheckList.add(false)
		val layoutManager =  LinearLayoutManager(this)
		layoutManager.orientation = LinearLayoutManager.HORIZONTAL
		binding.flameBtnList.layoutManager = layoutManager
		binding.flameBtnList.adapter = ListAdapterHorizontal(flameList, visibleCheckList)

		val fadeInOutAnimationInExtra = AnimationUtils.loadAnimation(applicationContext,
			R.anim.fade_inandout
		)
		fadeInOutAnimationInExtra.setAnimationListener (
			object : Animation.AnimationListener {
				override fun onAnimationStart(animation: Animation) {
					binding.extraOptionUseBtn.isEnabled = false
				}
				override fun onAnimationEnd(animation: Animation) {
					binding.extraOptionUseBtn.isEnabled = true
				}
				override fun onAnimationRepeat(animation: Animation) {
				}
			}
		)

		class ListViewHolder2(val binding: BeforeAfterChooseHoriBinding): RecyclerView.ViewHolder(binding.root)
		class ListAdapterHorizontal2(val datas: ArrayList<Int>):
			RecyclerView.Adapter<RecyclerView.ViewHolder>(){
			override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
				return ListViewHolder2(BeforeAfterChooseHoriBinding.inflate(LayoutInflater.from(parent.context), parent, false))
			}
			override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
				val bindingViewHolder2 = (holder as ListViewHolder2).binding
				if(position == 1) {
					bindingViewHolder2.optionInfoText.text = extraOptionText(afterExtraStat)
					if(binding.extraOptionUseBtn.text != "STOP")
						bindingViewHolder2.optionInfoText.startAnimation(fadeInOutAnimationInExtra)
					bindingViewHolder2.optionChoiceBtn.isEnabled = (binding.extraOptionUseBtn.text == "사용하기")
					bindingViewHolder2.optionChoiceBtn.setOnClickListener {
						itItem.extraStat = afterExtraStat
						runBlocking {
							CoroutineScope(Dispatchers.IO).launch {
								itemsDao.updateExtraOption(itItem.extraStat, itItem.extraArmIgn, itItem.itemId)
							}.join()
						}
						isFirst = true
						imsiArrayList = ArrayList<Int>(listOf(1))
						binding.extraInfoTextList.layoutManager = GridLayoutManager(this@ExtraOptionActivity, imsiArrayList.size)
						binding.extraInfoTextList.adapter = ListAdapterHorizontal2(imsiArrayList)
					}
				}
				else if(position == 0) {
					if(datas.size == 1)
						bindingViewHolder2.optionInfoText.text = extraOptionText(afterExtraStat)
					else {
						bindingViewHolder2.optionChoiceBtn.isEnabled = (binding.extraOptionUseBtn.text == "사용하기")
						bindingViewHolder2.optionInfoText.text = extraOptionText(beforeExtraStat)
					}
					bindingViewHolder2.optionInfoText.visibility = View.VISIBLE
					if(!isFirst && datas.size == 1 && binding.extraOptionUseBtn.text != "STOP")
						bindingViewHolder2.optionInfoText.startAnimation(fadeInOutAnimationInExtra)
					if(datas.size == 1) bindingViewHolder2.optionChoiceBtn.visibility = View.INVISIBLE
					else bindingViewHolder2.optionChoiceBtn.visibility = View.VISIBLE
					bindingViewHolder2.optionChoiceBtn.setOnClickListener {
						itItem.extraStat = beforeExtraStat
						afterExtraStat = beforeExtraStat
						isFirst = true
						imsiArrayList = ArrayList<Int>(listOf(1))
						binding.extraInfoTextList.layoutManager = GridLayoutManager(this@ExtraOptionActivity, imsiArrayList.size)
						binding.extraInfoTextList.adapter = ListAdapterHorizontal2(imsiArrayList)
					}
				}
				isFirst = false
			}
			override fun getItemCount(): Int {
				return datas.size
			}
		}
		var layoutManager2 =  GridLayoutManager(this, imsiArrayList.size)
		binding.extraInfoTextList.layoutManager = layoutManager2
		binding.extraInfoTextList.adapter = ListAdapterHorizontal2(imsiArrayList)

		val fadeInOutAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_inandout)
		fadeInOutAnimation.setAnimationListener (
			object : Animation.AnimationListener {
				override fun onAnimationStart(animation: Animation) {
					binding.extraOptionUseBtn.isEnabled = false
				}
				override fun onAnimationEnd(animation: Animation) {
					binding.extraOptionUseBtn.isEnabled = true
				}
				override fun onAnimationRepeat(animation: Animation) {
				}
			}
		)

		fun cancelAuto() {
			runBlocking {
				CoroutineScope(Dispatchers.Main).launch {
					binding.autoOptionChangeCheckBox.isEnabled = true
					binding.extraOptionUseBtn.text = "사용하기"
				}.join()
			}
		}

		binding.extraOptionUseBtn.setOnClickListener {
			if(binding.autoOptionChangeCheckBox.isChecked && binding.extraOptionUseBtn.text == "사용하기") {
				binding.autoOptionChangeCheckBox.isEnabled = false
				binding.extraOptionUseBtn.text = "STOP"
				timer(name = "extraOptionUseTimer", period = 25, initialDelay = 100) {
					if(!isResumedActivity && !isHomeButtonPressed) cancel()
					itItem.useCash[10 + selectedFlameId]++
					runBlocking {
						CoroutineScope(Dispatchers.IO).launch {
							itemsDao.updateUseInfo(
								itItem.useMeso,
								itItem.useCash,
								itItem.destroyNum,
								itItem.itemId
							)
						}.join()
					}

					beforeExtraStat = itItem.extraStat
					itItem.extraOptionChange(selectedFlameId)
					afterExtraStat = itItem.extraStat

					when (selectedFlameId) {
						1, 2 -> {
							imsiArrayList = ArrayList<Int>(listOf(1))
							runBlocking {
								CoroutineScope(Dispatchers.IO).launch {
									itemsDao.updateExtraOption(itItem.extraStat, itItem.extraArmIgn, itItem.itemId)
								}.join()
							}
						}
						3 -> {
							itItem.extraStat = beforeExtraStat
							imsiArrayList = ArrayList<Int>(listOf(1, 2))
						}
					}
					if(binding.extraOptionUseBtn.text != "STOP") {
						cancelAuto()
						cancel()
					}
					var extraStatTotalValue = when(selectedWantedOption) {
						0, 1, 2, 3 -> {
							afterExtraStat[selectedWantedOption * 2] + afterExtraStat[13] * 10
						}
						else -> {
							0
						}
					}
					extraStatTotalValue += when(selectedWantedOption) {
						0, 1, 3 -> {
							afterExtraStat[8] * 4
						}
						2 -> {
							afterExtraStat[10] * 4
						}
						4 -> {
							afterExtraStat[8]
						}
						else -> {
							afterExtraStat[10]
						}
					}
					runBlocking {
						CoroutineScope(Dispatchers.Main).launch {
							layoutManager2 = GridLayoutManager(this@ExtraOptionActivity, imsiArrayList.size)
							binding.extraInfoTextList.layoutManager = layoutManager2
							binding.extraInfoTextList.adapter = ListAdapterHorizontal2(imsiArrayList)
						}.join()
					}
					when(selectedWantedOption) {
						0, 1, 2, 3 -> {
							if(extraStatTotalValue >= wantedValueList[selectedWantedValue].replace("급", "").toInt()) {
								runBlocking {
									CoroutineScope(Dispatchers.Main).launch {
										binding.extraOptionUseBtn.text = "사용하기"
										layoutManager2 = GridLayoutManager(this@ExtraOptionActivity, imsiArrayList.size)
										binding.extraInfoTextList.layoutManager = layoutManager2
										binding.extraInfoTextList.adapter = ListAdapterHorizontal2(imsiArrayList)
									}.join()
								}
								cancelAuto()
								cancel()
							}
						}
						else -> {
							if(extraStatTotalValue == ceil(Math.max(itItem.baseStat[8], itItem.baseStat[10]).toDouble() / 100 * (itItem.itemLvl/40 + 1) * 7 * Math.pow(1.1, (7-3).toDouble())).toInt()) {
								runBlocking {
									CoroutineScope(Dispatchers.Main).launch {
										binding.extraOptionUseBtn.text = "사용하기"
										layoutManager2 = GridLayoutManager(this@ExtraOptionActivity, imsiArrayList.size)
										binding.extraInfoTextList.layoutManager = layoutManager2
										binding.extraInfoTextList.adapter = ListAdapterHorizontal2(imsiArrayList)
									}.join()
								}
								cancelAuto()
								cancel()
							}
						}
					}
				}
			} else if(binding.extraOptionUseBtn.text == "STOP"){
				binding.extraOptionUseBtn.text = "사용하기"
			} else if(!binding.autoOptionChangeCheckBox.isChecked) {
				itItem.useCash[10 + selectedFlameId]++
				runBlocking {
					CoroutineScope(Dispatchers.IO).launch {
						itemsDao.updateUseInfo(
							itItem.useMeso,
							itItem.useCash,
							itItem.destroyNum,
							itItem.itemId
						)
					}.join()
				}
				beforeExtraStat = itItem.extraStat

				itItem.extraOptionChange(selectedFlameId)
				afterExtraStat = itItem.extraStat
				when (selectedFlameId) {
					1, 2 -> {
						imsiArrayList = ArrayList<Int>(listOf(1))
						runBlocking {
							CoroutineScope(Dispatchers.IO).launch {
								itemsDao.updateExtraOption(itItem.extraStat, itItem.extraArmIgn, itItem.itemId)
							}.join()
						}
					}
					3 -> {
						itItem.extraStat = beforeExtraStat
						imsiArrayList = ArrayList<Int>(listOf(1, 2))
					}
				}
				layoutManager2 = GridLayoutManager(this, imsiArrayList.size)
				binding.extraInfoTextList.layoutManager = layoutManager2
				binding.extraInfoTextList.adapter = ListAdapterHorizontal2(imsiArrayList)

			}
		} // 환생의 불꽃 버튼 클릭 시
	}

	override fun onResume() {
		super.onResume()
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


	private fun extraOptionText(extraStat: MutableList<Int>): SpannableStringBuilder {
		val builder: SpannableStringBuilder = SpannableStringBuilder("")
		builder.append(extraOptionTextHelper("STR", extraStat[0]))
		builder.append(extraOptionTextHelper("DEX", extraStat[2]))
		builder.append(extraOptionTextHelper("INT", extraStat[4]))
		builder.append(extraOptionTextHelper("LUK", extraStat[6]))
		builder.append(extraOptionTextHelper("최대 Hp", extraStat[20]))
		builder.append(extraOptionTextHelper("최대 Mp", extraStat[22]))
		builder.append(extraOptionTextHelper("공격력", extraStat[8]))
		builder.append(extraOptionTextHelper("마력", extraStat[10]))
		builder.append(extraOptionTextHelper("방어력", extraStat[24]))
		builder.append(extraOptionTextHelper("이동속도", extraStat[15]))
		builder.append(extraOptionTextHelper("점프력", extraStat[14]))
		builder.append(extraOptionTextHelper("보스 몬스터 공격 시 데미지", extraStat[19]))
		builder.append(extraOptionTextHelper("데미지", extraStat[18]))
		builder.append(extraOptionTextHelper("올스탯", extraStat[13]))
		builder.append(extraOptionTextHelper("착용 제한 레벨 감소", extraStat[16]))
		return builder
	}

	private fun extraOptionTextHelper(name: String, extraValue: Int): SpannableStringBuilder {
		if (extraValue == 0)
			return SpannableStringBuilder("")
		val returnString = SpannableStringBuilder("$name : ")
		var text = if(name == "착용 제한 레벨 감소")
			"-$extraValue"
		else
			"+$extraValue"
		text = plusPercent(text, name)
		text += "\n"
		val valueString: SpannableStringBuilder = SpannableStringBuilder(text)
		val colorExtraSpan = ForegroundColorSpan(Color.GREEN)
		valueString.setSpan(colorExtraSpan, 0,
			valueString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
		returnString.append(valueString)
		return returnString
	} // 장비 설명창 도움

	private fun plusPercent(text: String, name: String): String {
		return if(name === "몬스터 방어율 무시" || name === "올스탯" || name === "데미지" ||
			name === "보스 몬스터 공격 시 데미지") "$text%"
		else text
	} // %를 써야하는 스텟들
}