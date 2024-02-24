package com.maple_simulator_and_enchantor.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.maple_simulator_and_enchantor.AppData
import com.maple_simulator_and_enchantor.Item
import com.maple_simulator_and_enchantor.R
import com.maple_simulator_and_enchantor.database.Database
import com.maple_simulator_and_enchantor.database.ItemsDao
import com.maple_simulator_and_enchantor.databinding.ActivityEnchantBinding
import com.maple_simulator_and_enchantor.databinding.BeforeAfterChooseHoriBinding
import com.maple_simulator_and_enchantor.databinding.ListItemHoriBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.security.SecureRandom
import java.util.*
import kotlin.collections.ArrayList

class EnchantActivity : AppCompatActivity() {
	lateinit var db: Database
	lateinit var binding: ActivityEnchantBinding
	lateinit var itItem: Item
	var selectedScrollId: Int = 0
	var selectedScrollInId: Int = 0
	private lateinit var aboutInventoryIntent: Intent
	private lateinit var itemsDao: ItemsDao
	private val visibleCheckList = ArrayList<Boolean>()
	private lateinit var beforeEnchantStat: MutableList<Int>
	private var afterEnchantStat = List<Int>(100){0}.toMutableList()
	private var selectedText = ""
	var isFirst = false
	private val spellTypeList = listOf(0, R.string.amazing_pos_chaos, R.string.amazing_enforce, R.string.innocent,
			R.string.gold_hammer, R.string.magical, R.string.miracle, R.string.trace_spell, R.string.pure_white)
	private val scrollList = arrayListOf(
		R.drawable.scroll_chaos, R.drawable.scroll_amazing,
		R.drawable.scroll_innocent, R.drawable.scroll_goldhammer,
		R.drawable.scroll_magical, R.drawable.scroll_miracle,
		R.drawable.scroll_magic, R.drawable.scroll_white)
	private val traceSpellList: List<List<Int>> = listOf(
			listOf(0, R.array.traceSpellWeaponWarriorList, R.array.traceSpellWeaponArcherList,
			R.array.traceSpellWeaponMagicianList, R.array.traceSpellWeaponChiefList,
			R.array.traceSpellWeaponPirateList, R.array.traceSpellWeaponCommonList),
			listOf(0, R.array.traceSpellNoWeaponWarriorList, R.array.traceSpellNoWeaponArcherList,
				R.array.traceSpellNoWeaponMagicianList, R.array.traceSpellNoWeaponChiefList,
				R.array.traceSpellNoWeaponPirateList, R.array.traceSpellNoWeaponCommonList))
	private var enchantTextArrayList = ArrayList<SpannableStringBuilder>()

	@SuppressLint("DiscouragedApi")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityEnchantBinding.inflate(layoutInflater)
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

		binding.enchantOptionEquipImg.setImageResource(resources.getIdentifier(
			"id${itItem.itemCode}", "drawable", packageName))
		binding.enchantOptionEquipNameText.text = itItem.name

		beforeEnchantStat = itItem.enchantStat
		afterEnchantStat = itItem.enchantStat

		binding.enchantOptionUseBtn.isEnabled = false
		binding.chaosAutoLayout.visibility = View.INVISIBLE


		class ListViewHolder(val binding: ListItemHoriBinding): RecyclerView.ViewHolder(binding.root)
		class ListAdapterHorizontal(val datas: ArrayList<Int>):
			RecyclerView.Adapter<RecyclerView.ViewHolder>(){
			override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
				return ListViewHolder(ListItemHoriBinding.inflate(LayoutInflater.from(parent.context), parent, false))
			}
			override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
				val bindingViewHolder = (holder as ListViewHolder).binding
				if(visibleCheckList[position]) bindingViewHolder.checkBtn.visibility = View.VISIBLE
				else bindingViewHolder.checkBtn.visibility = View.INVISIBLE
				bindingViewHolder.listItemImg.setImageResource(datas[position])
				bindingViewHolder.listItemImg.setOnClickListener {
					if (binding.enchantOptionUseBtn.text != "STOP") {
						if (visibleCheckList[position]) {
							visibleCheckList[position] = false
							notifyItemChanged(position)
							binding.enchantOptionUseBtn.isEnabled = false
							selectedScrollId = 0; selectedScrollInId = 0
						} else {
							val spellTypeArray = getSpellTypeArray(position + 1)
							AlertDialog.Builder(this@EnchantActivity)
								.setTitle(
									if (spellTypeList[selectedScrollId] != R.string.trace_spell) "목록"
									else "피버 타임, 손재주 만렙, 길드스킬 +4%"
								)
								.setItems(spellTypeArray) { _, which ->
									selectedScrollId = position + 1
									binding.enchantOptionUseBtn.isEnabled = true
									selectedScrollInId = which + 1
									selectedText = spellTypeArray[which]
									if(selectedText == "놀라운 긍정의 혼돈 주문서(100%, 리턴 스크롤)")
										binding.chaosAutoLayout.visibility = View.VISIBLE
									else {
										binding.chaosAutoLayout.visibility = View.INVISIBLE
										binding.autoOptionChangeCheckBox.isChecked = false
									}
									for (i in 0 until visibleCheckList.size)
										if (visibleCheckList[i]) {
											visibleCheckList[i] = false
											notifyItemChanged(i)
										}
									visibleCheckList[position] = true
									notifyItemChanged(position)
								}.show()
						}
					}
				}
			}
			override fun getItemCount(): Int {
				return datas.size
			}

			fun getSpellTypeArray(pos: Int): Array<String> {
				return resources.getStringArray(
					when(spellTypeList[pos]) {
						R.string.amazing_pos_chaos -> R.array.amazingPosChaosList
						R.string.amazing_enforce -> R.array.amazEnforceList
						R.string.innocent -> R.array.innocentList
						R.string.gold_hammer -> R.array.goldHammerList
						R.string.magical -> R.array.magicalList
						R.string.trace_spell -> {
							when(itItem.itemType % 100) {
								0, 1 -> traceSpellList[0][itItem.itemType / 100] // 무기, 보조무기
								19 -> R.array.traceSpellWeaponCommonList // 기계심장
								3, 4, 5, 6, 7, 9, 10, 21 -> traceSpellList[1][itItem.itemType / 100]
								8 -> {
									when(itItem.itemType / 100) {
										3 -> R.array.traceSpellGlovesSplList
										else -> R.array.traceSpellGlovesAtkList
									}
								} // 장갑
								11, 12, 13, 15, 17, 18 -> traceSpellList[1][itItem.itemType / 100]// 나중에 공용에 체력 추가해야됨
								else -> R.array.amazingPosChaosList
							}
						}
						R.string.pure_white -> R.array.pureWhiteList
						R.string.miracle -> R.array.miracleList
						else -> R.array.amazingPosChaosList
					})
			}
		}

		for(i in scrollList)
			visibleCheckList.add(false)

		val layoutManager =  LinearLayoutManager(this)
		layoutManager.orientation = LinearLayoutManager.HORIZONTAL
		binding.enchantBtnList.layoutManager = layoutManager
		binding.enchantBtnList.adapter = ListAdapterHorizontal(scrollList)

		val fadeInOutAnimationInEnchant = AnimationUtils.loadAnimation(applicationContext,
			R.anim.fade_inandout
		)
		fadeInOutAnimationInEnchant.setAnimationListener (
			object : Animation.AnimationListener {
				override fun onAnimationStart(animation: Animation) {
					binding.enchantOptionUseBtn.isEnabled = false
				}
				override fun onAnimationEnd(animation: Animation) {
					binding.enchantOptionUseBtn.isEnabled = true
				}
				override fun onAnimationRepeat(animation: Animation) {}
			}
		)

		class ListViewHolder2(val binding: BeforeAfterChooseHoriBinding): RecyclerView.ViewHolder(binding.root)
//		class ListAdapterHorizontal2(val datas: ArrayList<SpannableStringBuilder>):
//			RecyclerView.Adapter<RecyclerView.ViewHolder>(){
//			override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//				return ListViewHolder2(BeforeAfterChooseHoriBinding.inflate(LayoutInflater.from(parent.context), parent, false))
//			}
//			override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//				val bindingViewHolder2 = (holder as ListViewHolder2).binding
//				if(position == 1) {
//					itItem.remainUpGrade--
//					bindingViewHolder2.optionInfoText.text = enchantOptionText(beforeEnchantStat, afterEnchantStat)
//					itItem.remainUpGrade++
//					bindingViewHolder2.optionInfoText.startAnimation(fadeInOutAnimationInEnchant)
//					bindingViewHolder2.optionChoiceBtn.setOnClickListener {
//						itItem.enchantStat = afterEnchantStat
//						itItem.remainUpGrade--
//						updateEnchant()
//						isFirst = true
//					}
//				}
//				else if(position == 0) {
//					if(datas.size == 1) {
//						if(spellTypeList[selectedScrollId] == R.string.amazing_pos_chaos && (selectedScrollInId == 1 || selectedScrollInId == 3))
//							bindingViewHolder2.optionInfoText.text = enchantOptionText(beforeEnchantStat, afterEnchantStat)
//						else
//							bindingViewHolder2.optionInfoText.text = enchantOptionText(afterEnchantStat)
//					}
//					else
//						bindingViewHolder2.optionInfoText.text = enchantOptionText(beforeEnchantStat)
//					bindingViewHolder2.optionInfoText.visibility = View.VISIBLE
//					if(!isFirst && datas.size == 1) {
//						bindingViewHolder2.optionInfoText.startAnimation(fadeInOutAnimationInEnchant)
//					}
//
//					if(datas.size == 1) bindingViewHolder2.optionChoiceBtn.visibility = View.INVISIBLE
//					else bindingViewHolder2.optionChoiceBtn.visibility = View.VISIBLE
//					bindingViewHolder2.optionChoiceBtn.setOnClickListener {
//						itItem.enchantStat = beforeEnchantStat
//						afterEnchantStat = beforeEnchantStat
//						isFirst = true
//						imsiArrayList = ArrayList<Int>(listOf(1))
//						binding.enchantInfoTextList.layoutManager = GridLayoutManager(this@EnchantActivity, imsiArrayList.size)
//						binding.enchantInfoTextList.adapter = ListAdapterHorizontal2(imsiArrayList)
//					}
//				}
//				isFirst = false
//			}
//			override fun getItemCount(): Int {
//				return datas.size
//			}
//		}
		class ListAdapterHorizontal2(val datas: ArrayList<SpannableStringBuilder>):
			RecyclerView.Adapter<RecyclerView.ViewHolder>(){

			fun resizeItems(spellType: Int, sucFailDest: String) {
				if((spellType == R.string.amazing_pos_chaos && (selectedScrollInId == 2 || selectedScrollInId == 4)) ||
					(spellType == R.string.magical && (selectedScrollInId == 2 || selectedScrollInId == 4))) {
					Log.d("asd", "asd")
					if(sucFailDest == "success" && datas.size == 1) {
						datas.add(datas[0])
						notifyItemInserted(1)
					} else if(sucFailDest == "fail" && datas.size == 2) {
						datas.removeAt(1)
						notifyItemRemoved(1)
					}
				} else {
					if(datas.size == 2) {
						datas.removeAt(1)
						notifyItemRemoved(1)
					}
				}
			}

			fun editItem() {
				if(datas.size == 2) {
					itItem.remainUpGrade--
					datas[datas.size - 1] = enchantOptionText(beforeEnchantStat, afterEnchantStat)
					itItem.remainUpGrade++
				} else
					datas[datas.size-1] = enchantOptionText(afterEnchantStat)
				notifyItemChanged(datas.size-1)
			}

			override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
				return ListViewHolder2(BeforeAfterChooseHoriBinding.inflate(LayoutInflater.from(parent.context), parent, false))
			}

			override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
				val bindingViewHolder2 = (holder as ListViewHolder2).binding
				bindingViewHolder2.optionChoiceBtn.setOnClickListener {
					binding.enchantOptionUseBtn.isEnabled = true
					if(position == 0) {
						itItem.enchantStat = beforeEnchantStat
						afterEnchantStat = beforeEnchantStat
					} else if(position == 1) {
						itItem.enchantStat = afterEnchantStat
						itItem.remainUpGrade--
						updateEnchant()
					}
					if(datas.size == 2)
						datas.removeAt(1)
					binding.enchantInfoTextList.layoutManager = GridLayoutManager(this@EnchantActivity, datas.size)
					editItem()
				}

				bindingViewHolder2.optionChoiceBtn.visibility = if(datas.size == 2) View.VISIBLE else View.INVISIBLE
				bindingViewHolder2.optionChoiceBtn.isEnabled = binding.enchantOptionUseBtn.text == "사용하기"
				bindingViewHolder2.optionInfoText.text = datas[position]
				if(position == datas.size - 1) {
					if(binding.enchantOptionUseBtn.text == "사용하기" && isFirst) {
						bindingViewHolder2.optionInfoText.startAnimation(fadeInOutAnimationInEnchant)
						isFirst = false
					}
				}
			}
			override fun getItemCount(): Int {
				return datas.size
			}
		}

		enchantTextArrayList = ArrayList(listOf(enchantOptionText(beforeEnchantStat)))
		binding.enchantInfoTextList.itemAnimator = null
		binding.enchantInfoTextList.layoutManager = GridLayoutManager(this@EnchantActivity, enchantTextArrayList.size)
		val mAdapter = ListAdapterHorizontal2(enchantTextArrayList)
		binding.enchantInfoTextList.adapter = mAdapter

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
					resource.setLoopCount(1)
					val callback = object : Animatable2Compat.AnimationCallback() {
						override fun onAnimationEnd(drawable: Drawable?) {
							super.onAnimationEnd(drawable)
							binding.enchantEffect.visibility = View.INVISIBLE
						}
					}
					resource.registerAnimationCallback(callback)
					return false
				}
			}
		}

		fun amazingPosChaosOneUse(): String {
			val secureRandom = SecureRandom()
			val probStat = listOf(1838, 5139, 7526, 8913, 9407, 10000)
			val changeEnchantStat = MutableList<Int>(100) {0}
			for(i in listOf(0, 2, 4, 6, 8, 10, 24, 14, 15, 20, 22)) {
				changeEnchantStat[i] += if (itItem.totalStat[i] != 0) {
					val prob = secureRandom.nextInt(10000) + 1
					var changeStat = 0
					for (j in 0 until 6) {
						if (prob <= probStat[j]) {
							changeStat = if (j == 5) 6 else j
							break
						}
					}
					if (i == 20 || i == 22) changeStat *= 10
					changeStat
				} else {
					0
				}
			}
			when(selectedScrollInId) {
				2, 4 -> {
					itItem.useCash[30]++ // 리턴 스크롤
					return if(itItem.enchant(changeEnchantStat, when(selectedScrollInId){2->100; 4->60; else->100})) {
						afterEnchantStat = itItem.enchantStat
						itItem.enchantStat = beforeEnchantStat
						itItem.remainUpGrade++
						"success"
					} else {
						afterEnchantStat = itItem.enchantStat
						updateEnchant()
						"fail"
					}
				} // 놀라운 긍정의 혼돈 주문서 100%, 60% (리턴 스크롤)
				1, 3 -> {
					val sucOrFail = itItem.enchant(changeEnchantStat, when(selectedScrollInId){1->100; 3->60; else->100})
					afterEnchantStat = itItem.enchantStat
					updateEnchant()
					return if(sucOrFail) "success" else "fail"
				} // 놀라운 긍정의 혼돈 주문서 100%, 60% (리턴 스크롤 X)
			}
			return "success"
		}

		fun goldHammerOneUse(): String {
			val sucOrFail = itItem.goldHammer(when (selectedScrollInId) {1 -> 100; 2 -> 50; else -> 100})
			afterEnchantStat = itItem.enchantStat
			updateEnchant()
			return if(sucOrFail) "success" else "fail"
		}

		fun innocentOneUse(): String {
			val sucOrFail = itItem.innocent(when(selectedScrollInId){1, 2->0; 3, 4->1; else->0}, when(selectedScrollInId){1,3->100; 2,4->60; else->100})
			afterEnchantStat = itItem.enchantStat
			updateEnchant()
			return if(sucOrFail) "success" else "fail"
		}

		fun pureWhiteOneUse(): String {
			val sucOrFail = itItem.whiteEnchant(when (selectedScrollInId) {1 -> 100; 2 -> 10; else -> 100 })
			afterEnchantStat = itItem.enchantStat
			updateEnchant()
			return if(sucOrFail) "success" else "fail"
		}

		fun traceSpellOneUse(): String {
			val changeEnchantStat = MutableList<Int>(100) {0}
			val prob = // 성공 확률
				if(selectedText.contains("100%")) 100
				else if(selectedText.contains("70%")) 100
				else if(selectedText.contains("30%")) 59
				else 39
			val indexAtkSpl = if(selectedText.contains("공격력")) 8 else 10
			val indexStat = if(selectedText.contains("힘")) 0
			else if(selectedText.contains("민첩")) 2
			else if(selectedText.contains("지능")) 4
			else if(selectedText.contains("행운")) 6
			else if(selectedText.contains("체력")) 20
			else 0
			var changeAtkSpl = 0
			var changeStat = 0
			when(itItem.itemType % 100) {
				0, 1, 19 -> {
					if(itItem.itemLvl <= 70) {
						if(selectedText.contains("100%")) {changeAtkSpl = 1}
						else if(selectedText.contains("70%")) {changeAtkSpl = 2}
						else if(selectedText.contains("30%")) {changeAtkSpl = 3; changeStat = if(indexStat == 20) 50 else 1}
						else if(selectedText.contains("15%")) {changeAtkSpl = 5; changeStat = if(indexStat == 20) 100 else 2}
					} else if(itItem.itemLvl <= 110) {
						if(selectedText.contains("100%")) {changeAtkSpl = 2}
						else if(selectedText.contains("70%")) {changeAtkSpl = 3; changeStat = if(indexStat == 20) 50 else 1}
						else if(selectedText.contains("30%")) {changeAtkSpl = 5; changeStat = if(indexStat == 20) 100 else 2}
						else if(selectedText.contains("15%")) {changeAtkSpl = 7; changeStat = if(indexStat == 20) 150 else 3}
					} else {
						if(selectedText.contains("100%")) {changeAtkSpl = 3; changeStat = if(indexStat == 20) 50 else 1}
						else if(selectedText.contains("70%")) {changeAtkSpl = 5; changeStat = if(indexStat == 20) 100 else 2}
						else if(selectedText.contains("30%")) {changeAtkSpl = 7; changeStat = if(indexStat == 20) 150 else 3}
						else if(selectedText.contains("15%")) {changeAtkSpl = 9; changeStat = if(indexStat == 20) 200 else 4}
					}
				}
				3, 4, 5, 6, 7, 9, 10, 21 -> {
					if(itItem.itemLvl <= 70) {
						if(selectedText.contains("100%")) {changeStat = if(indexStat == 20) 50 else 1; changeEnchantStat[24] = 1; changeEnchantStat[20] += 5}
						else if(selectedText.contains("70%")) {changeStat = if(indexStat == 20) 100 else 2; changeEnchantStat[24] = 2; changeEnchantStat[20] += 15}
						else if(selectedText.contains("30%")) {changeStat = if(indexStat == 20) 150 else 3; changeEnchantStat[24] = 4; changeEnchantStat[20] += 30}
						else if(selectedText.contains("15%")) {changeStat = if(indexStat == 20) 200 else 4; changeEnchantStat[24] = 6; changeEnchantStat[20] += 45}
					} else if(itItem.itemLvl <= 110) {
						if(selectedText.contains("100%")) {changeStat = if(indexStat == 20) 100 else 2; changeEnchantStat[24] = 2; changeEnchantStat[20] += 20}
						else if(selectedText.contains("70%")) {changeStat = if(indexStat == 20) 150 else 3; changeEnchantStat[24] = 4; changeEnchantStat[20] += 40}
						else if(selectedText.contains("30%")) {changeStat = if(indexStat == 20) 250 else 5; changeEnchantStat[24] = 7; changeEnchantStat[20] += 70}
						else if(selectedText.contains("15%")) {changeStat = if(indexStat == 20) 350 else 7; changeEnchantStat[24] = 10; changeEnchantStat[20] += 110}
					} else {
						if(selectedText.contains("100%")) {changeStat = if(indexStat == 20) 150 else 3; changeEnchantStat[24] = 3; changeEnchantStat[20] += 30}
						else if(selectedText.contains("70%")) {changeStat = if(indexStat == 20) 200 else 4; changeEnchantStat[24] = 5; changeEnchantStat[20] += 70}
						else if(selectedText.contains("30%")) {changeStat = if(indexStat == 20) 350 else 7; changeEnchantStat[24] = 10; changeEnchantStat[20] += 120}
						else if(selectedText.contains("15%")) {changeStat = if(indexStat == 20) 500 else 10; changeEnchantStat[24] = 15; changeEnchantStat[20] += 170}
					}
				}
				8 -> {
					if(itItem.itemLvl <= 70) {
						if(selectedText.contains("100%")) {changeEnchantStat[24] += 3}
						else if(selectedText.contains("70%")) {changeAtkSpl = 1}
						else if(selectedText.contains("30%")) {changeAtkSpl = 2}
						else if(selectedText.contains("15%")) {changeAtkSpl = 3}
					} else {
						if(selectedText.contains("100%")) {changeAtkSpl = 1}
						else if(selectedText.contains("70%")) {changeAtkSpl = 2}
						else if(selectedText.contains("30%")) {changeAtkSpl = 3}
						else if(selectedText.contains("15%")) {changeAtkSpl = 4}
					}
				}
				11, 12, 13, 15, 17, 18 -> {
					if(itItem.itemLvl <= 70) {
						if(selectedText.contains("100%")) {changeStat = if(indexStat == 20) 50 else 1;}
						else if(selectedText.contains("70%")) {changeStat = if(indexStat == 20) 100 else 2;}
						else if(selectedText.contains("30%")) {changeStat = if(indexStat == 20) 150 else 3;}
					} else if(itItem.itemLvl <= 110) {
						if(selectedText.contains("100%")) {changeStat = if(indexStat == 20) 50 else 1;}
						else if(selectedText.contains("70%")) {changeStat = if(indexStat == 20) 100 else 2;}
						else if(selectedText.contains("30%")) {changeStat = if(indexStat == 20) 200 else 4;}
					} else {
						if(selectedText.contains("100%")) {changeStat = if(indexStat == 20) 100 else 2;}
						else if(selectedText.contains("70%")) {changeStat = if(indexStat == 20) 150 else 3;}
						else if(selectedText.contains("30%")) {changeStat = if(indexStat == 20) 250 else 5;}
					}
				}
			}
			changeEnchantStat[indexStat] += changeStat
			changeEnchantStat[indexAtkSpl] += changeAtkSpl
			val sucOrFail = itItem.enchant(changeEnchantStat, prob)
			afterEnchantStat = itItem.enchantStat
			updateEnchant()
			return if(sucOrFail) "success" else "fail"
		}

		fun magicalOneUse(): String {
			val secureRandom = SecureRandom()
			val probStat = listOf(1000, 5000, 10000)
			val changeEnchantStat = MutableList<Int>(100) {0}
			val index = when(selectedScrollInId) {1, 2 -> 8 else -> 10}
			val prob = secureRandom.nextInt(10000) + 1
			var changeStat = 0
			for (j in 0 until 3) {
				if (prob <= probStat[j]) {
					changeStat = if (j == 2) 9 else 11 - j
					break
				}
			}
			changeEnchantStat[index] = changeStat
			when(selectedScrollInId) {
				2, 4 -> {
					itItem.useCash[30]++ // 리턴 스크롤
					return if(itItem.enchant(changeEnchantStat, 100)) {
						afterEnchantStat = itItem.enchantStat
						itItem.enchantStat = beforeEnchantStat
						itItem.remainUpGrade++
						val dj = Glide.with(this@EnchantActivity).asGif().load(R.raw.enchant_success_effect)
						dj.listener(listener).centerInside().into(binding.enchantEffect)
						"success"
					} else {
						afterEnchantStat = itItem.enchantStat
						isFirst = true
						updateEnchant()
						"fail"
					}
				} // 매지컬 주문서 100%, 60% (리턴 스크롤)
				1, 3 -> {
					val sucOrFail = itItem.enchant(changeEnchantStat, 100)
					afterEnchantStat = itItem.enchantStat
					updateEnchant()
					return if(sucOrFail) "success" else "fail"
				} // 매지컬 주문서 100%, 60% (리턴 스크롤 X)
			}
			return "success"
		}

		fun amazingEnforceOneUse(): String {
			val protected = when(selectedScrollInId) {
				1 -> false
				else -> true
			}
			val sucFailDest = itItem.amazingEnchant(protected)
			runBlocking {
				CoroutineScope(Dispatchers.IO).launch {
					if(!itItem.canStarForce)
						itemsDao.updateCanStarForce(1, itItem.itemId)
					else
						itemsDao.updateCanStarForce(0, itItem.itemId)
					itemsDao.updateStarStat(itItem.maxStar, itItem.currentStar, itItem.starStat, itItem.starArmIgn, itItem.itemId) // 스타포스 데이터 업데이트
				}.join()
			}
			updateEnchant()
			return when(sucFailDest) {1->"success"; 0->"fail"; else->"destroy"}
		}

		fun miracleOneUse(): String {
			val secureRandom = SecureRandom()
			val probStat = listOf(3000, 10000)
			val changeEnchantStat = MutableList<Int>(100) {0}
			val index = when(selectedScrollInId) {1 -> 8 else -> 10}
			val prob = secureRandom.nextInt(10000) + 1
			var changeStat = 0
			for (j in 0 until 2) {
				if (prob <= probStat[j]) {
					changeStat = if (j == 0) 3 else 2
					break
				}
			}
			changeEnchantStat[index] = changeStat
			val sucFail = itItem.enchant(changeEnchantStat, 50)
			afterEnchantStat = itItem.enchantStat
			updateEnchant()
			return if(sucFail) "success" else "fail"
		}

		fun enchantEffect(sucFailDest: String) {
			binding.enchantEffect.visibility = View.VISIBLE
			val dj = when(sucFailDest) {
				"success" -> Glide.with(this@EnchantActivity).asGif().load(R.raw.enchant_success_effect)
				"fail" -> Glide.with(this@EnchantActivity).asGif().load(R.raw.enchant_fail_effect)
				else -> Glide.with(this@EnchantActivity).asGif().load(R.raw.enchant_destroy_effect)
			}
			dj.listener(listener).centerInside().into(binding.enchantEffect)
		}

		@SuppressLint("NotifyDataSetChanged")
		fun manageAuto(startOrEnd: String) {
			binding.autoOptionChangeCheckBox.isEnabled = startOrEnd != "start"
			binding.enchantOptionUseBtn.text = if(startOrEnd == "start") "STOP" else "사용하기"
			mAdapter.notifyDataSetChanged()
		}

		val autoEnchantTimer = Timer()
		var wantedType = "공격력+6"
		binding.wantedOptionTypeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("공격력+6", "마력+6"))
		binding.wantedOptionTypeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				if(position == 0) {
					wantedType = "공격력+6"
				} else if(position == 1) {
					wantedType = "마력+6"
				}
			}
			override fun onNothingSelected(parent: AdapterView<*>?) {
			}
		}

		binding.enchantOptionUseBtn.setOnClickListener {
			val spellType = spellTypeList[selectedScrollId]
			if(itItem.remainUpGrade == 0  && (spellType == R.string.amazing_pos_chaos ||
					spellType == R.string.magical || spellType == R.string.trace_spell)) {
				Toast.makeText(this@EnchantActivity, "업그레이드 가능 횟수가 0입니다.", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			} else if(itItem.upGradeRestore == 0  && spellType == R.string.pure_white) {
				Toast.makeText(this@EnchantActivity, "복구 가능 횟수가 0입니다.", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			} else if(itItem.goldHammer == 1  && spellType == R.string.gold_hammer) {
				Toast.makeText(this@EnchantActivity, "황금 망치가 이미 적용된 장비입니다.", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			} else if(itItem.amazing == 1 && spellType == R.string.innocent && (selectedScrollInId == 3 || selectedScrollInId == 4)) {
				Toast.makeText(this@EnchantActivity, "이 아이템에는 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			} else if(itItem.itemLvl > 150 && spellType == R.string.amazing_enforce) {
				Toast.makeText(this@EnchantActivity, "150제 이하 장비에만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			} else if(spellType == R.string.amazing_enforce) {
				if(itItem.currentStar >= 15) {
					Toast.makeText(this@EnchantActivity, "15성 미만 장비에만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
					return@setOnClickListener
				} else if(selectedScrollInId == 2 && itItem.currentStar >= 12) {
					Toast.makeText(this@EnchantActivity, "12성 이상 장비에는 프로텍트 주문서를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
					return@setOnClickListener
				} else if(itItem.currentStar >= itItem.maxStar) {
					Toast.makeText(this@EnchantActivity, "최대 강화입니다.", Toast.LENGTH_SHORT).show()
					return@setOnClickListener
				}
			} else if(itItem.itemType % 100 != 0 && itItem.itemType % 100 != 19 && itItem.itemType % 100 != 1 && spellType == R.string.magical) {
				Toast.makeText(this@EnchantActivity, "무기와 기계 심장에만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			} else if(spellType == R.string.miracle) {
				when(itItem.itemType % 100) {
					3, 4, 5, 6, 7, 8, 9, 21 -> {}
					else -> {
						Toast.makeText(this@EnchantActivity, "방어구에만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
						return@setOnClickListener
					}
				}
			}
			var sucFailDest = "success"

			if(spellType == R.string.trace_spell)
				itItem.useCash[20 + selectedScrollId]++
			beforeEnchantStat = itItem.enchantStat
			isFirst = true
			when(spellType) {
				R.string.amazing_pos_chaos -> {
					if(binding.enchantOptionUseBtn.text == "STOP"){
						binding.enchantOptionUseBtn.text = "일시정지"
					} else if(binding.autoOptionChangeCheckBox.isChecked) {
						manageAuto("start")
						val autoEnchantTimerTask = object: TimerTask() {
							override fun run() {
								sucFailDest = amazingPosChaosOneUse()
								runBlocking {
									CoroutineScope(Dispatchers.Main).launch() {
										mAdapter.editItem()
									}.join()
								}
								if(binding.enchantOptionUseBtn.text != "STOP" ||
									(wantedType == "공격력+6" && afterEnchantStat[8] - beforeEnchantStat[8] >= 6) ||
									(wantedType == "마력+6" && afterEnchantStat[10] - beforeEnchantStat[10] >= 6)){
									runOnUiThread {
										enchantEffect(sucFailDest)
										manageAuto("end")
									}
									cancel()
								}
							}
						}
						autoEnchantTimer.schedule(autoEnchantTimerTask, 100, 50)
					} else if(!binding.autoOptionChangeCheckBox.isChecked) {
						sucFailDest = amazingPosChaosOneUse()
					}
				} // 놀라운 긍정의 혼돈 주문서
				R.string.innocent -> sucFailDest = innocentOneUse() // 이노센트 주문서
				R.string.pure_white -> sucFailDest = pureWhiteOneUse() // 순백의 주문서
				R.string.gold_hammer -> sucFailDest = goldHammerOneUse() // 황금망치
				R.string.magical -> sucFailDest = magicalOneUse() // 매지컬 주문서
				R.string.trace_spell -> sucFailDest = traceSpellOneUse() // 주문의 흔적
				R.string.amazing_enforce -> sucFailDest = amazingEnforceOneUse() // 놀라운 장비강화 주문서
				R.string.miracle -> sucFailDest = miracleOneUse() // 미라클 주문서
			}
			if(!binding.autoOptionChangeCheckBox.isChecked)
				enchantEffect(sucFailDest)
			mAdapter.resizeItems(spellType, sucFailDest)
			binding.enchantInfoTextList.layoutManager =
				GridLayoutManager(this@EnchantActivity, mAdapter.itemCount)
			mAdapter.editItem()
			runBlocking {
				CoroutineScope(Dispatchers.IO).launch {
					itemsDao.updateUseInfo(itItem.useMeso, itItem.useCash, itItem.destroyNum, itItem.itemId)
				}.join()
			}
		} // 주문서 버튼 클릭 시
	}

	private fun updateEnchant() {
		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				itemsDao.updateEnchant(
					itItem.remainUpGrade, itItem.upGradeRestore, itItem.goldHammer, itItem.enchantStat, itItem.amazing, itItem.itemId
				)
			}.join()
		}
	} // 주문서 스탯 업데이트

	private val abilityList = AppData.abilityOptionTypeList

	private fun enchantOptionText(enchantStat: MutableList<Int>): SpannableStringBuilder {
		val builder: SpannableStringBuilder = SpannableStringBuilder("")
		for(i in listOf(0, 2, 4, 6, 20, 22, 8, 10, 24, 15, 14))
			builder.append(enchantOptionTextHelper(abilityList[i], enchantStat[i]))
		val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.unique_yellow))
		builder.append("업그레이드 가능 횟수: ${itItem.remainUpGrade}")
		val restoreString = SpannableStringBuilder(" (복구 가능 횟수 : ${itItem.upGradeRestore})\n")
		restoreString.setSpan(colorSpan, 0, restoreString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
		builder.append(restoreString)
		if(itItem.goldHammer == 1) {
			builder.append("황금망치 제련 적용\n")
		}
		return builder
	}

	private fun enchantOptionText(beforeEnchantStat: MutableList<Int>, afterEnchantStat: MutableList<Int>): SpannableStringBuilder {
		val builder: SpannableStringBuilder = SpannableStringBuilder("")
		for(i in listOf(0, 2, 4, 6, 20, 22, 8, 10, 24, 15, 14))
			builder.append(enchantOptionTextHelper(abilityList[i], beforeEnchantStat[i], afterEnchantStat[i]))
		val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.unique_yellow))
		builder.append("업그레이드 가능 횟수: ${itItem.remainUpGrade}")
		val restoreString = SpannableStringBuilder(" (복구 가능 횟수 : ${itItem.upGradeRestore})\n")
		restoreString.setSpan(colorSpan, 0, restoreString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
		builder.append(restoreString)
		if(itItem.goldHammer == 1) {
			builder.append("황금망치 제련 적용\n")
		}
		return builder
	}

	private fun enchantOptionTextHelper(name: String, enchantValue: Int): SpannableStringBuilder {
		if (enchantValue == 0)
			return SpannableStringBuilder("")
		val returnString = SpannableStringBuilder("$name : ")
		var text = if(name == "착용 제한 레벨 감소")
			"-$enchantValue"
		else
			"+$enchantValue"
		text += "\n"
		val valueString: SpannableStringBuilder = SpannableStringBuilder(text)
		val colorExtraSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.rare_blue))
		valueString.setSpan(colorExtraSpan, 0,
			valueString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
		returnString.append(valueString)
		return returnString
	} // 장비 설명창 도움

	private fun enchantOptionTextHelper(name: String, beforeEnchantValue: Int, afterEnchantValue: Int): SpannableStringBuilder {
		if (afterEnchantValue == 0)
			return SpannableStringBuilder("")
		val returnString = SpannableStringBuilder("$name : ")
		var text = "+$afterEnchantValue (+${afterEnchantValue-beforeEnchantValue})"
		text += "\n"
		val valueString: SpannableStringBuilder = SpannableStringBuilder(text)
		val colorExtraSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.rare_blue))
		valueString.setSpan(colorExtraSpan, 0,
			valueString.length-4-afterEnchantValue.toString().length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
		returnString.append(valueString)
		return returnString
	} // 장비 설명창 도움
}

