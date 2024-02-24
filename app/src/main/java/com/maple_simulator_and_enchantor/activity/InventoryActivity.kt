package com.maple_simulator_and_enchantor.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog.show
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.maple_simulator_and_enchantor.AppData
import com.maple_simulator_and_enchantor.R

import com.maple_simulator_and_enchantor.adapter_and_ui.SquareImageView
import com.maple_simulator_and_enchantor.database.Database
import com.maple_simulator_and_enchantor.database.Items
import com.maple_simulator_and_enchantor.database.ItemsDao
import com.maple_simulator_and_enchantor.databinding.ActivityInventoryBinding
import com.maple_simulator_and_enchantor.databinding.InventoryGridLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class InventoryActivity : AppCompatActivity() {
	lateinit var db: Database
	lateinit var itemsDao: ItemsDao
	lateinit var allItems: List<Items>
	lateinit var binding: ActivityInventoryBinding
	private var characterId: Int = 0
	private val inventorySize = 2000

	var itemSize = 0 // 아이템 개수
	private lateinit var requestLauncher: ActivityResultLauncher<Intent>
	private lateinit var itemList: MutableList<Items>
	private lateinit var itemTypeMap: MutableMap<Int, Boolean>
	private val selectedItemIndex = ArrayList<Int>()

	@SuppressLint("DiscouragedApi", "Recycle")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityInventoryBinding.inflate(layoutInflater)
		characterId = intent.getIntExtra("characterId", 0)
		setContentView(binding.root)
		db = Database.getInstance(applicationContext)!!  // 데이터베이스 생성
		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				itemsDao = db.itemsDao()
				allItems = itemsDao.getAll()
			}.join()
		}



		binding.equipmentCreateBtn.setOnClickListener {
			val intent: Intent = Intent(this, EquipmentCreateActivity::class.java)
			intent.putExtra("characterId", characterId)
			requestLauncher.launch(intent)
		} // 장비 추가

		class InventoryViewHolder(val binding: InventoryGridLayoutBinding) :
			RecyclerView.ViewHolder(binding.root)

		class InventoryAdapter() :
			RecyclerView.Adapter<RecyclerView.ViewHolder>() {
			override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
				return InventoryViewHolder(
					InventoryGridLayoutBinding.inflate(
						LayoutInflater.from(parent.context),
						parent,
						false
					)
				)
			}

			@SuppressLint("NotifyDataSetChanged")
			override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
				val bindingViewHolder = (holder as InventoryViewHolder).binding
				if (position < itemList.size) {
					if (characterId == itemList[position].wearCharacterId) {
						bindingViewHolder.background.setBackgroundResource(R.drawable.inventory_real_base2)
					} else {
						bindingViewHolder.background.setBackgroundResource(R.drawable.inventory_real_base1)
					}
					bindingViewHolder.itemImage.visibility = View.VISIBLE
					bindingViewHolder.itemImage.setImageResource(
						resources.getIdentifier(
							"id${itemList[position].itemCode}",
							"drawable", packageName
						)
					) // imageView 아이템 코드 읽어서 사진 띄우기
					bindingViewHolder.itemImage.setOnClickListener {
						val intent: Intent = Intent(this@InventoryActivity, ItemActivity::class.java)
						intent.putExtra("itemId", itemList[position].id)
						intent.putExtra("characterId", characterId)
						requestLauncher.launch(intent)
					}
				} else {
					bindingViewHolder.background.setBackgroundResource(R.drawable.inventory_real_base1)
					bindingViewHolder.itemImage.visibility = View.INVISIBLE
				}
			}

			override fun getItemCount(): Int {
				return inventorySize
			}
		}

		val equipmentTypeItems = arrayOf(
			"무기", "보조무기", "엠블렘", "모자", "상의", "하의", "상하의",
			"신발", "장갑", "망토", "어깨장식", "눈장식", "얼굴장식", "귀고리", "포켓", "벨트", "훈장",
			"반지", "펜던트", "기계심장", "벳지", "방패"
		)
		itemTypeMap = emptyMap<Int, Boolean>().toMutableMap()

		for ((cnt, i) in equipmentTypeItems.withIndex()) {
			itemTypeMap[cnt] = true
			selectedItemIndex.add(cnt)
		}

		refresh()

		binding.equipmentTypeSelectBtn.setOnClickListener {
			val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
			val eventHandler = DialogInterface.OnClickListener { p0, p1 ->
				if (p1 == DialogInterface.BUTTON_POSITIVE) {
					for (i in equipmentTypeItems.indices) {
						itemTypeMap[i] = selectedItemIndex.contains(i)
					}
					refresh()
					binding.inventoryList.layoutManager = GridLayoutManager(this@InventoryActivity, 4)
					binding.inventoryList.adapter = InventoryAdapter()
				} // Yes를 눌렀을 때
				else if (p1 == DialogInterface.BUTTON_NEUTRAL) {
					selectedItemIndex.clear()
					for (cnt in equipmentTypeItems.indices) {
						itemTypeMap[cnt] = true
						selectedItemIndex.add(cnt)
					}
					dialog.setMultiChoiceItems(
						equipmentTypeItems,
						itemTypeMap.values.toBooleanArray()
					) { dialogInterface: DialogInterface, i: Int, b:
					Boolean ->
						if (b) {
							selectedItemIndex.add(i)
						} else if (selectedItemIndex.contains(i)) {
							selectedItemIndex.remove(i)
						}
					}
					refresh()
					binding.inventoryList.layoutManager = GridLayoutManager(this@InventoryActivity, 4)
					binding.inventoryList.adapter = InventoryAdapter()
				} // 초기화를 눌렀을 때
				else if (p1 == DialogInterface.BUTTON_NEGATIVE) {
					selectedItemIndex.clear()
					for (cnt in equipmentTypeItems.indices) {
						itemTypeMap[cnt] = false
					}
					dialog.setMultiChoiceItems(
						equipmentTypeItems,
						itemTypeMap.values.toBooleanArray()
					) { dialogInterface: DialogInterface, i: Int, b:
					Boolean ->
						if (b) {
							selectedItemIndex.add(i)
						} else if (selectedItemIndex.contains(i)) {
							selectedItemIndex.remove(i)
						}
					}
					refresh()
					binding.inventoryList.layoutManager = GridLayoutManager(this@InventoryActivity, 4)
					binding.inventoryList.adapter = InventoryAdapter()
				} // 초기화를 눌렀을 때
			}
			dialog.setMultiChoiceItems(
				equipmentTypeItems,
				itemTypeMap.values.toBooleanArray()
			) { dialogInterface: DialogInterface, i: Int, b:
			Boolean ->
				if (b) {
					selectedItemIndex.add(i)
				} else if (selectedItemIndex.contains(i)) {
					selectedItemIndex.remove(i)
				}
			}
			dialog.setTitle("장비 보기 설정")
			dialog.setPositiveButton("확인", eventHandler)
			dialog.setNegativeButton("모두 취소", eventHandler)
			dialog.setNeutralButton("모두 선택", eventHandler)
			dialog.show()



		} // 장비 종류 선택

		requestLauncher = registerForActivityResult(
			ActivityResultContracts.StartActivityForResult()
		) {
			refresh()
			binding.inventoryList.layoutManager = GridLayoutManager(this, 4)
			binding.inventoryList.adapter = InventoryAdapter()
		} // 액티비티 끝나고 돌아왔을 때 실행될 Callback

		binding.inventoryList.layoutManager = GridLayoutManager(this, 4)
		binding.inventoryList.adapter = InventoryAdapter()
	}

	@SuppressLint("Range", "DiscouragedApi", "Recycle")
	private fun refresh() {
		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				allItems = itemsDao.getAll() // 캐릭터 인벤토리 초기화
			}.join()
		}

		itemList = emptyList<Items>().toMutableList()
		val itemNotWearList = emptyList<Items>().toMutableList()
		for (item in allItems) {
			if (item.wearCharacterId != 0 && item.wearCharacterId != characterId) continue
			if (!selectedItemIndex.contains(item.itemType%100)) continue
			if (item.wearCharacterId == 0) itemNotWearList.add(item)
			else itemList.add(item)
		}
		itemList.addAll(itemNotWearList)
		binding.equipmentCreateBtn.isEnabled =
			(allItems.size <= inventorySize) // 인벤토리가 꽉찼을 경우 장비 생성 비활성화
	}
}

