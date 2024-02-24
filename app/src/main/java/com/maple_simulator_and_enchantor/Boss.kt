package com.maple_simulator_and_enchantor

import kotlin.properties.Delegates

class Boss(var koreanName: String, var englishName: String, var armor: Double, var maxHP: Long, var dropItems: List<String>, var info: String) {
}

class BossInfo {
	companion object {
		private val englishNameToIdMap: Map<String, Int> =
			mapOf(
				"chaos_pierre" to 7,
				"chaos_banban" to 8,
				"chaos_bloodyqueen" to 9,
				"normal_swoo" to 13,
				"normal_damien" to 14,
				"normal_slime" to 15,
				"normal_lucid" to 16,
				"normal_will" to 17,
				"normal_jinhilla" to 18,
				"easy_kalos" to 20)
		private val idToAllMap: Map<Int, Boss> =
			mapOf(
				getBossIdByBossName("chaos_pierre") to Boss("카오스 피에르", "chaos_pierre", 80.0, 80000000000L,
					listOf("트릭스터 워리어팬츠", "트릭스터 레인져팬츠", "트릭스터 던위치팬츠", "트릭스터 어새신팬츠", "트릭스터 원더러팬츠"),
					"최초 격파 시 루타비스 세트(하의) 해금"),
				getBossIdByBossName("chaos_banban") to Boss("카오스 반반", "chaos_banban", 100.0, 100000000000L,
					listOf("이글아이 워리어아머", "이글아이 레인져후드", "이글아이 던위치로브", "이글아이 어새신셔츠", "이글아이 원더러코트"),
				"최초 격파 시 루타비스 세트(상의) 해금"),
				getBossIdByBossName("chaos_bloodyqueen") to Boss("카오스 블러디 퀸", "chaos_bloodyqueen", 120.0, 140000000000L,
					listOf("하이네스 워리어헬름", "하이네스 레인져베레", "하이네스 던위치햇", "하이네스 어새신보닛", "하이네스 원더러햇"),
					"최초 격파 시 루타비스 세트(모자) 해금"),
				getBossIdByBossName("normal_swoo") to Boss("노말 스우", "normal_swoo", 300.0, 1500000000000L,
					listOf("앱솔랩스 나이트글러브", "앱솔랩스 아처글러브", "앱솔랩스 메이지글러브", "앱솔랩스 시프글러브", "앱솔랩스 파이렛글러브",
						"앱솔랩스 나이트슈즈", "앱솔랩스 아처슈즈", "앱솔랩스 메이지슈즈", "앱솔랩스 시프슈즈", "앱솔랩스 파이렛슈즈",
						"앱솔랩스 나이트케이프", "앱솔랩스 아처케이프", "앱솔랩스 메이지케이프", "앱솔랩스 시프케이프", "앱솔랩스 파이렛케이프"),
					"최초 격파 시 앱솔랩스 세트(장갑, 신발, 망토) 해금"),
				getBossIdByBossName("normal_damien") to Boss("노말 데미안", "normal_damien", 300.0, 1200000000000L,
					listOf("앱솔랩스 나이트숄더", "앱솔랩스 아처숄더", "앱솔랩스 메이지숄더", "앱솔랩스 시프숄더", "앱솔랩스 파이렛숄더",
						"앱솔랩스 나이트헬름", "앱솔랩스 아처후드", "앱솔랩스 메이지크라운", "앱솔랩스 시프캡", "앱솔랩스 파이렛페도라"),
					"최초 격파 시 앱솔랩스 세트(모자, 어깨장식) 해금"),
				getBossIdByBossName("normal_slime") to Boss("노말 가디언 엔젤 슬라임", "normal_slime", 300.0, 5000000000000L,
					listOf("여명의 가디언 엔젤 링"),
					"최초 격파 시 여명의 가디언 엔젤 링 해금"),
				getBossIdByBossName("normal_lucid") to Boss("노말 루시드", "normal_lucid", 300.0, 24000000000000L,
					listOf("아케인셰이드 나이트글러브", "아케인셰이드 아처글러브", "아케인셰이드 메이지글러브", "아케인셰이드 시프글러브", "아케인셰이드 파이렛글러브",
						"아케인셰이드 나이트슈즈", "아케인셰이드 아처슈즈", "아케인셰이드 메이지슈즈", "아케인셰이드 시프슈즈", "아케인셰이드 파이렛슈즈",
						"아케인셰이드 나이트케이프", "아케인셰이드 아처케이프", "아케인셰이드 메이지케이프", "아케인셰이드 시프케이프", "아케인셰이드 파이렛케이프"),
					"최초 격파 시 아케인셰이드 세트(장갑, 신발, 망토) 해금"),
				getBossIdByBossName("normal_will") to Boss("노말 윌", "normal_will", 300.0, 25200000000000L,
					listOf("아케인셰이드 나이트숄더", "아케인셰이드 아처숄더", "아케인셰이드 메이지숄더", "아케인셰이드 시프숄더", "아케인셰이드 파이렛숄더"),
					"최초 격파 시 아케인셰이드 세트(어깨장식) 해금"),
				getBossIdByBossName("normal_jinhilla") to Boss("노말 진 힐라", "normal_jinhilla", 300.0, 88000000000000L,
					listOf("데이브레이크 펜던트"),
					"최초 격파 시 데이브레이크 펜던트 해금"),
				getBossIdByBossName("easy_kalos") to Boss("이지 칼로스", "easy_kalos", 330.0, 357000000000000L,
					listOf("에테르넬 나이트헬름", "에테르넬 아처햇", "에테르넬 메이지햇", "에테르넬 시프반다나", "에테르넬 파이렛햇",
						"에테르넬 나이트아머", "에테르넬 아처후드", "에테르넬 메이지로드", "에테르넬 시프셔츠", "에테르넬 파이렛코트",
						"에테르넬 나이트팬츠", "에테르넬 아처팬츠", "에테르넬 메이지팬츠", "에테르넬 시프팬츠", "에테르넬 파이렛팬츠",
					  "에테르넬 나이트숄더", "에테르넬 아처숄더", "에테르넬 메이지숄더", "에테르넬 시프숄더", "에테르넬 파이렛숄더"),
					"최초 격파 시 에테르넬 세트(모자, 상의, 하의, 어깨장식) 해금")
		)
		fun getBossIdByBossName(name: String): Int {
			return englishNameToIdMap[name]!!
		}
		fun getBossByBossId(id: Int): Boss {
			return idToAllMap[id]!!
		}
	}


}