package com.maple_simulator_and_enchantor.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.maple_simulator_and_enchantor.Item
import com.maple_simulator_and_enchantor.R
import com.maple_simulator_and_enchantor.database.Database
import com.maple_simulator_and_enchantor.databinding.ActivityStarForceBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.timer

class StarForceActivity : AppCompatActivity() {
	lateinit var db: Database
	lateinit var binding: ActivityStarForceBinding
	lateinit var itItem: Item
	var selectedButtonName: String = ""
	private lateinit var aboutInventoryIntent: Intent
	private var starCatchBtn = false
	private var notDestroyBtn = true
	private var costSale30Btn = false
	private var success100Btn = false
	var chanceTime = 0

	private val listener: RequestListener<GifDrawable> by lazy {
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
						binding.starForceEffect.visibility = View.INVISIBLE
						refresh()
					}
				}
				resource.registerAnimationCallback(callback)
				return false
			}
		}
	}

	private lateinit var djSuccess: RequestBuilder<GifDrawable>
	private lateinit var djFail: RequestBuilder<GifDrawable>
	private lateinit var djDestroy: RequestBuilder<GifDrawable>
	private lateinit var djSuccessAuto: RequestBuilder<GifDrawable>
	private lateinit var djFailAuto: RequestBuilder<GifDrawable>
	private lateinit var djDestroyAuto: RequestBuilder<GifDrawable>


	@SuppressLint("DiscouragedApi")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityStarForceBinding.inflate(layoutInflater)
		setContentView(binding.root)
		aboutInventoryIntent = intent
		db = Database.getInstance(applicationContext)!!  // 데이터베이스 생성

		djSuccess = Glide.with(this).asGif()
			.listener(listener).centerInside().skipMemoryCache(true).load(R.raw.enchant_success_effect)
		djSuccessAuto = Glide.with(this).asGif()
			.listener(listener).centerInside().skipMemoryCache(true).load(R.raw.enchant_success_effect_auto)
		djFail = Glide.with(this).asGif()
			.listener(listener).centerInside().skipMemoryCache(true).load(R.raw.enchant_fail_effect)
		djFailAuto = Glide.with(this).asGif()
			.listener(listener).centerInside().skipMemoryCache(true).load(R.raw.enchant_fail_effect_auto)
		djDestroy = Glide.with(this).asGif()
			.listener(listener).centerInside().skipMemoryCache(true).load(R.raw.enchant_destroy_effect)
		djDestroyAuto = Glide.with(this).asGif()
			.listener(listener).centerInside().skipMemoryCache(true).load(R.raw.enchant_destroy_effect_auto)

		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				val itemsDao = db.itemsDao()
				val itemData = itemsDao.getById(aboutInventoryIntent.getIntExtra("itemId", 0))[0]
				val cubeTables = db.cubeTableDao().getAll()
				itItem = Item(itemsDao, itemData, cubeTables)
			}.join()
		}

		binding.starForceEquipImg.setImageResource(resources.getIdentifier(
			"id${itItem.itemCode}", "drawable", packageName))
		binding.starForceEquipNameText.text = itItem.name

		binding.enforceBtn.setOnClickListener {
			if(binding.autoStarForceCheckBox.isChecked && binding.enforceBtn.text == "강화하기") {
				binding.enforceBtn.text = "STOP"
				timer(period = 50, initialDelay = 100) {
					if(binding.enforceBtn.text == "강화하기") {
						cancel()
					}
					val beforeStar = itItem.currentStar
					val sucFailDest = itItem.starForceEnforce(starCatchBtn, notDestroyBtn, costSale30Btn, success100Btn, chanceTime == 2)
					runBlocking {
						CoroutineScope(Dispatchers.Main).launch {
							binding.starForceEffect.visibility = View.VISIBLE
							if(this@StarForceActivity.isFinishing) cancel()
							when(sucFailDest) {
								1 ->{
									djSuccessAuto.into(binding.starForceEffect)
									chanceTime = 0
								}
								2 ->{
									djFailAuto.into(binding.starForceEffect)
									if(beforeStar - itItem.currentStar == 1)
										chanceTime++
									else
										chanceTime = 0
								}
								3 ->{
									djDestroyAuto.into(binding.starForceEffect)
								}
							}
							if(itItem.currentStar >= itItem.maxStar ||
								itItem.currentStar >= 22) {
								binding.enforceBtn.text = "강화하기"
								binding.autoStarForceCheckBox.isChecked = false
								cancel()
							}
						}.join()
						if(binding.enforceBtn.text == "강화하기") {
							cancel()
						}
					}
				}
			} else if(binding.enforceBtn.text == "STOP") {
				binding.enforceBtn.text = "강화하기"
			} else {
				binding.enforceBtn.isEnabled = false
				val beforeStar = itItem.currentStar
				val sucFailDest = itItem.starForceEnforce(starCatchBtn, notDestroyBtn, costSale30Btn, success100Btn, chanceTime == 2)
				binding.starForceEffect.visibility = View.VISIBLE
				when(sucFailDest) {
					1 ->{
						djSuccess.into(binding.starForceEffect)
						chanceTime = 0
					}
					2 ->{
						djFail.into(binding.starForceEffect)
						if(beforeStar - itItem.currentStar == 1)
							chanceTime++
						else
							chanceTime = 0
					}
					3 ->{
						djDestroy.into(binding.starForceEffect)
					}
				}
			}

		}

		binding.notDestroyCheckBox.setOnCheckedChangeListener {
			_, isChecked ->
			notDestroyBtn = isChecked
			refresh()
		}
		binding.starCatchCheckBox.setOnCheckedChangeListener {
				_, isChecked ->
			starCatchBtn = isChecked
			refresh()
		}
		binding.costSale30CheckBox.setOnCheckedChangeListener {
				_, isChecked ->
			costSale30Btn = isChecked
			refresh()
		}
		binding.success100CheckBox.setOnCheckedChangeListener {
				_, isChecked ->
			success100Btn = isChecked
			refresh()
		}
		refresh()
	}

	@SuppressLint("DiscouragedApi")
	private fun refresh() {
		val starForceInfoText = binding.starForceInfoText
		when(itItem.maxStar == itItem.currentStar) {
			true -> {
				starForceInfoText.text = "최대 강화입니다."
				binding.enforceBtn.isEnabled = false
			}
			false -> {
				starForceInfoText.text =  itItem.nextStarForceText(starCatchBtn, notDestroyBtn, success100Btn, chanceTime == 2)
				binding.starForceCostText.text = itItem.nextStarForceCost(starCatchBtn, notDestroyBtn, costSale30Btn, success100Btn, chanceTime == 2)
				binding.enforceBtn.isEnabled = true
			}
		}
		binding.notDestroyCheckBox.isEnabled = !(itItem.currentStar < 15 || itItem.currentStar >= 17)
	}
}