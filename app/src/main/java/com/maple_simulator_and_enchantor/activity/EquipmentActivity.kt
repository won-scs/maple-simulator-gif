package com.maple_simulator_and_enchantor.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.maple_simulator_and_enchantor.Character
import com.maple_simulator_and_enchantor.R
import com.maple_simulator_and_enchantor.adapter_and_ui.SquareImageView
import com.maple_simulator_and_enchantor.database.*
import com.maple_simulator_and_enchantor.databinding.ActivityEquipmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class EquipmentActivity : AppCompatActivity() {
	lateinit var db: Database
	lateinit var itemsDao: ItemsDao
	private lateinit var itCharacter: Characters
	private lateinit var wearItems: List<Items>
	private lateinit var cubeTables: List<CubeTable>
	lateinit var binding: ActivityEquipmentBinding
	lateinit var itemTableDao: ItemTableDao
	private val requestLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) {
		refresh()
	}
	private val equipmentImageList: MutableList<Pair<ImageView, Int>> =
		emptyList<Pair<ImageView, Int>>().toMutableList()
	private var characterId = 0


	@SuppressLint("Recycle", "Range", "DiscouragedApi")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityEquipmentBinding.inflate(layoutInflater)
		setContentView(binding.root)
		characterId = intent.getIntExtra("characterId", 0)

		equipmentImageList.add(Pair(binding.weaponSlot, 0))
		equipmentImageList.add(Pair(binding.subWeaponSlot, 1))
		equipmentImageList.add(Pair(binding.emblemSlot, 2))
		equipmentImageList.add(Pair(binding.hatSlot, 3))
		equipmentImageList.add(Pair(binding.clothesSlot, 4))
		equipmentImageList.add(Pair(binding.pantsSlot, 5))
		equipmentImageList.add(Pair(binding.shoesSlot, 7))
		equipmentImageList.add(Pair(binding.glovesSlot, 8))
		equipmentImageList.add(Pair(binding.capeSlot, 9))
		equipmentImageList.add(Pair(binding.shoulderSlot, 10))
		equipmentImageList.add(Pair(binding.eyeAccSlot, 11))
		equipmentImageList.add(Pair(binding.foreheadSlot, 12))
		equipmentImageList.add(Pair(binding.earAccSlot, 13))
		equipmentImageList.add(Pair(binding.poketSlot, 14))
		equipmentImageList.add(Pair(binding.beltSlot, 15))
		equipmentImageList.add(Pair(binding.medalSlot, 16))
		equipmentImageList.add(Pair(binding.ring1Slot, 17))
		equipmentImageList.add(Pair(binding.ring2Slot, 117))
		equipmentImageList.add(Pair(binding.ring3Slot, 217))
		equipmentImageList.add(Pair(binding.ring4Slot, 317))
		equipmentImageList.add(Pair(binding.pendant1Slot, 18))
		equipmentImageList.add(Pair(binding.pendant2Slot, 118))
		equipmentImageList.add(Pair(binding.heartSlot, 19))
		equipmentImageList.add(Pair(binding.badgeSlot, 20))

		db = Database.getInstance(applicationContext)!!  // 데이터베이스 생성

		runBlocking {
			CoroutineScope(Dispatchers.IO).launch {
				itemsDao = db.itemsDao()
				itemTableDao = db.itemTableDao()
				cubeTables = db.cubeTableDao().getAll()
				itCharacter = db.charactersDao().getByCharacterId(characterId)[0]
			}.join()
		}

		for (i in 0 until equipmentImageList.size) {
			equipmentImageList[i].first.setOnClickListener {
				val character = Character(itCharacter, itemsDao, itemTableDao, cubeTables, characterId)
				runBlocking {
					CoroutineScope(Dispatchers.IO).launch {
						itemsDao = db.itemsDao()
						wearItems = itemsDao.getByWearCharacterId(characterId)
					}.join()
				}
				val slotType = equipmentImageList[i].second
				for(item in wearItems) {
					if(item.itemType % 100 == slotType % 100 || (item.itemType % 100 == 21 && slotType % 100 == 1)) {
						if(slotType % 100 == 17) {
							if(item.id != character.ringId[slotType/100])
								continue
						} else if(slotType % 100 == 18) {
							if(item.id != character.pendantId[slotType/100])
								continue
						}
						val intent: Intent = Intent(this, ItemActivity::class.java)
						intent.putExtra("itemId", item.id)
						intent.putExtra("itemCode", item.itemCode)
						intent.putExtra("characterId", characterId)
						requestLauncher.launch(intent)
						break
					}
				}
			}
		}

		binding.equipmentToInventoryBtn.setOnClickListener {
			val intent: Intent = Intent(this, InventoryActivity::class.java)
			intent.putExtra("characterId", characterId)
			requestLauncher.launch(intent)
		}
		refresh()
	}

	@SuppressLint("Range", "DiscouragedApi", "Recycle")
	private fun refresh() {
		runBlocking {
			db = Database.getInstance(applicationContext)!!  // 데이터베이스 생성
			CoroutineScope(Dispatchers.IO).launch {
				itemsDao = db.itemsDao()
				wearItems = itemsDao.getByWearCharacterId(characterId)
				itCharacter = db.charactersDao().getByCharacterId(characterId)[0]
			}.join()
		}

		for (i in 0 until equipmentImageList.size) {
			equipmentImageList[i].first.setImageResource(0)
			equipmentImageList[i].first.setBackgroundResource(R.drawable.inventory_base)
		} // 장비창 이미지 초기화

		for(item in wearItems) {
			val itemType = item.itemType
			val resource: Int = resources.getIdentifier("id${item.itemCode}", "drawable", packageName)
			val slotImageView: SquareImageView = when(itemType % 100) {
				0 -> binding.weaponSlot
				1, 21 -> binding.subWeaponSlot
				2 -> binding.emblemSlot
				3 -> binding.hatSlot
				4 -> binding.clothesSlot
				5 -> binding.pantsSlot
				6 -> binding.clothesSlot
				7 -> binding.shoesSlot
				8 -> binding.glovesSlot
				9 -> binding.capeSlot
				10 -> binding.shoulderSlot
				11 -> binding.eyeAccSlot
				12 -> binding.foreheadSlot
				13 -> binding.earAccSlot
				14 -> binding.poketSlot
				15 -> binding.beltSlot
				16 -> binding.medalSlot
				17 -> when(item.id) {
						itCharacter.ring1Id -> binding.ring1Slot
						itCharacter.ring2Id -> binding.ring2Slot
						itCharacter.ring3Id -> binding.ring3Slot
						itCharacter.ring4Id -> binding.ring4Slot
						else -> binding.ring1Slot
					}
				18 -> when(item.id) {
					itCharacter.pendant1Id -> binding.pendant1Slot
					itCharacter.pendant2Id -> binding.pendant2Slot
					else -> binding.pendant1Slot
					}
				19 -> binding.heartSlot
				20 -> binding.badgeSlot
				else -> binding.weaponSlot
			}
			slotImageView.setBackgroundResource(
				when(item.upAbilityList[0]){
					1-> R.drawable.equipment_rare
					2-> R.drawable.equipment_epic
					3-> R.drawable.equipment_unique
					4-> R.drawable.equipment_legendary
					else-> R.drawable.inventory_real_base1
					}
			)
			slotImageView.setImageResource(resource)
		}
	}
}