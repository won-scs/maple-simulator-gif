package com.maple_simulator_and_enchantor.activity

import android.annotation.SuppressLint
import android.app.LauncherActivity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build.VERSION_CODES.P
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.maple_simulator_and_enchantor.Boss
import com.maple_simulator_and_enchantor.BossInfo
import com.maple_simulator_and_enchantor.BossInfo.Companion.getBossIdByBossName
import com.maple_simulator_and_enchantor.R
import com.maple_simulator_and_enchantor.databinding.ActivityBossSelectBinding
import com.maple_simulator_and_enchantor.databinding.ListItemHoriBinding

class BossSelectActivity : AppCompatActivity() {
	lateinit var binding: ActivityBossSelectBinding
	private val visibleCheckList = ArrayList<Boolean>()
	private val bossList = ArrayList<Int>()
	private val bossNameList = ArrayList<Pair<String, String>>()
	private val bossInfoList = ArrayList<String>()
	private var selectedBossId = 0

	inner class ListViewHolder(val binding: ListItemHoriBinding): RecyclerView.ViewHolder(binding.root)
	inner class ListAdapterHorizontal(val datas: ArrayList<Int>, val visibleCheck: ArrayList<Boolean>):
		RecyclerView.Adapter<RecyclerView.ViewHolder>(){
		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
			return ListViewHolder(ListItemHoriBinding.inflate(LayoutInflater.from(parent.context), parent, false))
		}
		@SuppressLint("DiscouragedApi")
		override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
			val bindingViewHolder = (holder as ListViewHolder).binding
			if(visibleCheck[position]) bindingViewHolder.checkBtn.visibility = View.VISIBLE
			else bindingViewHolder.checkBtn.visibility = View.INVISIBLE
			bindingViewHolder.listItemImg.setImageResource(datas[position])
			bindingViewHolder.listItemImg.setOnClickListener {
				for (i in 0 until visibleCheckList.size) {
					if (i != position && visibleCheckList[i]) {
						visibleCheckList[i] = false
						notifyItemChanged(i)
					}
				}
				visibleCheckList[position] = !visibleCheckList[position]
				if(visibleCheckList[position]) {
					binding.bossName.text = bossNameList[position].first
					binding.bossInfo.text = bossInfoList[position]
					binding.bossImg.setImageResource(resources.getIdentifier(
						bossNameList[position].second, "drawable", packageName))
					selectedBossId = getBossIdByBossName(bossNameList[position].second)
				}
				notifyItemChanged(position)
				binding.bossSelectToDmgTestBtn.isEnabled = visibleCheckList[position]
			}
		}
		override fun getItemCount(): Int {
			return datas.size
		}
	}

	@SuppressLint("SetTextI18n", "DiscouragedApi")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityBossSelectBinding.inflate(layoutInflater)
		setContentView(binding.root)
		val characterId = intent.getIntExtra("characterId", 0)
		val requestLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
			ActivityResultContracts.StartActivityForResult()
		) {}

		binding.bossSelectToDmgTestBtn.setOnClickListener {
			val intent = Intent(this, DamageTestActivity::class.java)
			intent.putExtra("characterId", characterId)
			intent.putExtra("bossId", selectedBossId)
			requestLauncher.launch(intent)
		}

		bossList.add(R.drawable.chaos_pierre); bossNameList.add(Pair("카오스 피에르", "chaos_pierre"))
		bossInfoList.add("방어율 80%, HP: 800억"); visibleCheckList.add(false)
		bossList.add(R.drawable.chaos_banban); bossNameList.add(Pair("카오스 반반", "chaos_banban"))
		bossInfoList.add("방어율 100%, HP: 1000억"); visibleCheckList.add(false)
		bossList.add(R.drawable.chaos_bloodyqueen); bossNameList.add(Pair("카오스 블러디 퀸", "chaos_bloodyqueen"))
		bossInfoList.add("방어율 120%, HP: 1200억"); visibleCheckList.add(false)
		bossList.add(R.drawable.normal_swoo); bossNameList.add(Pair("노말 스우", "normal_swoo"))
		bossInfoList.add("방어율 300%, HP: 1조 5000억"); visibleCheckList.add(false)
		bossList.add(R.drawable.normal_damien); bossNameList.add(Pair("노말 데미안", "normal_damien"))
		bossInfoList.add("방어율 300%, HP: 1조 2000억"); visibleCheckList.add(false)
		bossList.add(R.drawable.normal_slime); bossNameList.add(Pair("노말 가디언 엔젤 슬라임", "normal_slime"))
		bossInfoList.add("방어율 300%, HP: 5조"); visibleCheckList.add(false)
		bossList.add(R.drawable.normal_lucid); bossNameList.add(Pair("노말 루시드", "normal_lucid"))
		bossInfoList.add("방어율 300%, HP: 24조"); visibleCheckList.add(false)
		bossList.add(R.drawable.normal_will); bossNameList.add(Pair("노말 윌", "normal_will"))
		bossInfoList.add("방어율 300%, HP: 25조 2000억"); visibleCheckList.add(false)
		bossList.add(R.drawable.normal_jinhilla); bossNameList.add(Pair("노말 진 힐라", "normal_jinhilla"))
		bossInfoList.add("방어율 300%, HP: 88조"); visibleCheckList.add(false)
		bossList.add(R.drawable.easy_kalos); bossNameList.add(Pair("이지 칼로스", "easy_kalos"))
		bossInfoList.add("방어율 330%, HP: 357조"); visibleCheckList.add(false)

		val layoutManager =  LinearLayoutManager(this)
		layoutManager.orientation = LinearLayoutManager.HORIZONTAL
		binding.bossList.layoutManager = layoutManager
		binding.bossList.adapter = ListAdapterHorizontal(bossList, visibleCheckList)
	}
}