package com.maple_simulator_and_enchantor.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.maple_simulator_and_enchantor.AppData
import com.maple_simulator_and_enchantor.InAppUpdate
import com.maple_simulator_and_enchantor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		binding.MainToCharacterSelectButton.setOnClickListener {
			val intent: Intent = Intent(this, CharacterSelectActivity::class.java)
			startActivity(intent)
		}

		binding.MainEndButton.setOnClickListener {
			finish()
		}
	}

	override fun onResume() {
		super.onResume()
	}

	override fun onDestroy() {
		super.onDestroy()
	}
}