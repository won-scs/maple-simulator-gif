package com.maple_simulator_and_enchantor

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdate(activity: Activity) : InstallStateUpdatedListener {

	private var appUpdateManager: AppUpdateManager
	private val MY_REQUEST_CODE = 500
	private var parentActivity: Activity = activity

	private var currentType = AppUpdateType.FLEXIBLE

	init {
		appUpdateManager = AppUpdateManagerFactory.create(parentActivity)
		appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
			// 업데이트를 해야하는 지 확인
			if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) { // 업데이트 해야함
				if (info.updatePriority() == 5) { // Priority: 5 (Immediate update flow)- 즉각적으로 진행
					if (info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
						startUpdate(info, AppUpdateType.IMMEDIATE)
					}
				} else if (info.updatePriority() == 4) { // Priority: 4
					val clientVersionStalenessDays = info.clientVersionStalenessDays()
					if (clientVersionStalenessDays != null && clientVersionStalenessDays >= 5 && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
						// Trigger IMMEDIATE flow - 즉각적으로 진행
						startUpdate(info, AppUpdateType.IMMEDIATE)
					} else if (clientVersionStalenessDays != null && clientVersionStalenessDays >= 3 && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
						// Trigger FLEXIBLE flow - 유연하게 진행
						startUpdate(info, AppUpdateType.FLEXIBLE)
					}
				} else if (info.updatePriority() == 3) { // Priority: 3
					val clientVersionStalenessDays = info.clientVersionStalenessDays()
					if (clientVersionStalenessDays != null && clientVersionStalenessDays >= 30 && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
						// Trigger IMMEDIATE flow - 즉각적으로 진행
						startUpdate(info, AppUpdateType.IMMEDIATE)
					} else if (clientVersionStalenessDays != null && clientVersionStalenessDays >= 15 && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
						// Trigger FLEXIBLE flow - 유연하게 진행
						startUpdate(info, AppUpdateType.FLEXIBLE)
					}
				} else if (info.updatePriority() == 2) { // Priority: 2
					val clientVersionStalenessDays = info.clientVersionStalenessDays()
					if (clientVersionStalenessDays != null && clientVersionStalenessDays >= 90 && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
						// Trigger IMMEDIATE flow - 즉각적으로 진행
						startUpdate(info, AppUpdateType.IMMEDIATE)
					} else if (clientVersionStalenessDays != null && clientVersionStalenessDays >= 30 && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
						// Trigger FLEXIBLE flow - 유연하게 진행
						startUpdate(info, AppUpdateType.FLEXIBLE)
					}
				} else if (info.updatePriority() == 1) { // Priority: 1
					// Trigger FLEXIBLE flow - 유연하게 진행
					startUpdate(info, AppUpdateType.FLEXIBLE)
				} else { // Priority: 0
					// 인앱 업데이트 안 보여줌
				}
			} else {
				// 업데이트 필요 없음
			}
		}
		appUpdateManager.registerListener(this)
	}


	private fun startUpdate(info: AppUpdateInfo, type: Int) {
		appUpdateManager.startUpdateFlowForResult(info, type, parentActivity, MY_REQUEST_CODE)
		currentType = type
	}

	fun onResume() {
		appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
			if (currentType == AppUpdateType.FLEXIBLE) {
				// 업데이트가 다운로드되었지만 설치되지 않은 경우 사용자에게 업데이트를 완료하도록 알립니다.
				if (info.installStatus() == InstallStatus.DOWNLOADED)
					flexibleUpdateDownloadCompleted()
			} else if (currentType == AppUpdateType.IMMEDIATE) {
				// IMMEDIATE(즉각적인)일때만 , 이미 업데이트 진행중인 상태입니다.
				if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
					startUpdate(info, AppUpdateType.IMMEDIATE)
				}
			}
		}
	}

	fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == MY_REQUEST_CODE) {
			if (resultCode != AppCompatActivity.RESULT_OK) {
				// 업데이트가 취소되거나 실패한 경우 업데이트를 다시 시작하도록 요청할 수 있습니다.
				Log.e("ERROR", "Update flow failed! Result code: $resultCode")
			}
		}
	}

	private fun flexibleUpdateDownloadCompleted() {
		Snackbar.make(
			parentActivity.findViewById(R.id.activity_main_layout),
			"An update has just been downloaded.",
			Snackbar.LENGTH_INDEFINITE
		).apply {
			setAction("RESTART") { appUpdateManager.completeUpdate() }
			setActionTextColor(Color.WHITE)
			show()
		}
	}

	fun onDestroy() {
		appUpdateManager.unregisterListener(this)
	}

	override fun onStateUpdate(state: InstallState) {
		if (state.installStatus() == InstallStatus.DOWNLOADED) {
			flexibleUpdateDownloadCompleted()
		}
	}

}