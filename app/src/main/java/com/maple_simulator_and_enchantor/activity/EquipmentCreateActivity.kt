package com.maple_simulator_and_enchantor.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.maple_simulator_and_enchantor.database.*
import com.maple_simulator_and_enchantor.databinding.ActivityEquipmentCreateBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class EquipmentCreateActivity : AppCompatActivity() {
	lateinit var db: Database
	lateinit var itemsDao: ItemsDao
	lateinit var itemTableDao: ItemTableDao
	lateinit var itemTable: ItemTable
	lateinit var itemTables: List<ItemTable>
	private lateinit var adapter: ArrayAdapter<String>
	private lateinit var itemNameList: ArrayList<String>

	@SuppressLint("Recycle", "Range", "DiscouragedApi")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivityEquipmentCreateBinding.inflate(layoutInflater)
		setContentView(binding.root)

		db = Database.getInstance(applicationContext)!!  // 데이터베이스 생성
		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				itemTableDao = db.itemTableDao()
				itemTables = itemTableDao.getAll()
				itemsDao = db.itemsDao()
			}.join()
		}
		val searchView: androidx.appcompat.widget.SearchView = binding.itemSearchView
		val characterId = intent.getIntExtra("characterId", 0)

		itemNameList = ArrayList<String>()
		for(i in itemTables) {
			if(i.canCreate == 1) {
				itemNameList.add(i.name)
			}
		}
		itemNameList.sort()

		adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, itemNameList)
		val listView =  binding.itemCreateList
		listView.adapter = adapter
		listView.setOnItemClickListener { adapterView: AdapterView<*>, view: View, position: Int, l: Long ->
			val data: String = adapterView.getItemAtPosition(position).toString()
			val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
			dialog.run {
				val eventHandler = DialogInterface.OnClickListener { p0, p1 ->
					if (p1 == DialogInterface.BUTTON_POSITIVE) {
						runBlocking {
							CoroutineScope(Dispatchers.IO).launch {
								itemTable = itemTableDao.getListByName(data)[0]
							}.join()
						}
						runBlocking {
							CoroutineScope(Dispatchers.IO).launch {
								val newItem = Items(
									0, itemTable.itemCode, itemTable.itemSetCode, itemTable.itemType, itemTable.itemLvl, itemTable.name, 0, characterId,
									itemTable.baseStat, itemTable.baseArmIgn, List(100) { 0 },
									0.0, List(100) { 0 }, 0.0, List(100) { 0 }, 0.0,
									itemTable.maxStar, 0, itemTable.upGrade, itemTable.upGrade, 0, 0,
									List(6){0}, List(6){0}, 0, List<Int>(100){0}, 0, itemTable.canFlame, itemTable.canCube,
									itemTable.canStarForce, 0, itemTable.canEnchant
								)
								if(itemTable.name == "블랙 하트") {
									newItem.upAbilityList = listOf(2, 0, 3019, 3017, 0, 0)
								}
								itemsDao.insert(newItem)
							}.join()
						}
						//finish()
					} // Yes를 눌렀을 때
					else if (p1 == DialogInterface.BUTTON_NEGATIVE) {
						Log.d("wow", "good choice")
					} // No를 눌렀을 때
				}
				setMessage("${data}\n선택하신 아이템이 맞습니까?")
				setPositiveButton("Yes", eventHandler)
				setNegativeButton("No", eventHandler)
				show()
			} // 선택한 아이템이 맞는지 Dialog 띄우기
		}

		searchView.setOnQueryTextListener(object :
			androidx.appcompat.widget.SearchView.OnQueryTextListener {
			@Override
			override fun onQueryTextSubmit(query: String?): Boolean {
				// 검색 버튼 누를 때 호출
				return true
			}

			@Override
			override fun onQueryTextChange(newText: String?): Boolean {
				// 검색창에서 글자가 변경이 일어날 때마다 호출
				val newItemNameList = ArrayList<String>()
				for (i in 0 until itemNameList.size) {
					if (itemNameList[i].contains(searchView.query.toString())) {
						newItemNameList.add(itemNameList[i])
					}
				}
				adapter = ArrayAdapter(this@EquipmentCreateActivity, android.R.layout.simple_list_item_1, newItemNameList)
				listView.adapter = adapter
				return true
			}
		})
	}
}