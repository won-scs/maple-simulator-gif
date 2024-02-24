package com.maple_simulator_and_enchantor

import android.app.Application
import android.text.SpannableStringBuilder
import android.view.animation.Animation
import android.view.animation.AnimationUtils

class AppData {


	companion object {
		val abilityOptionTypeList = listOf(
			"STR", "STR%", "DEX", "DEX%", "INT",
			"INT%", "LUK", "LUK%", "공격력", "공격력%",
			"마력", "마력%", "올스탯", "올스탯%", "점프력",
			"이동속도", "착용 레벨 제한 감소", "몬스터 방어율 무시%", "데미지%", "보스 몬스터 공격 시 데미지%",
			"최대 HP", "최대 HP%", "최대 MP", "최대 MP%", "방어력",
			"방어력%", "메소 획득량%", "아이템 드롭률%", "캐릭터 기준 9레벨 당 STR", "캐릭터 기준 9레벨 당 DEX",
			"캐릭터 기준 9레벨 당 INT", "캐릭터 기준 9레벨 당 LUK", "캐릭터 기준 9레벨 당 공격력", "캐릭터 기준 9레벨 당 마력",
			"크리티컬 데미지%", "모든 스킬의 재사용 대기시간",  "크리티컬 확률%",
			"<쓸만한 미스틱 도어> 스킬 사용 가능", "<쓸만한 어드밴스드 블레스> 스킬 사용 가능",
			"<쓸만한 하이퍼 바디> 스킬 사용 가능", "<쓸만한 샤프 아이즈> 스킬 사용 가능",
			"<쓸만한 윈드 부스터> 스킬 사용 가능", "<쓸만한 헤이스트> 스킬 사용 가능",
			"<쓸만한 컴뱃 오더스> 스킬 사용 가능", "피격 시 5% 확률로 데미지의 20% 무시",
			"피격 시 5% 확률로 데미지의 40% 무시", "피격 시 10% 확률로 데미지의 20% 무시",
			"피격 시 10% 확률로 데미지의 40% 무시", "모든 스킬의 MP 소모%", "HP 회복 아이템 및 회복 스킬 효율%",
			"피격 후 무적시간", "35% 확률로 받은 피해의 50%를 반사",
			"35% 확률로 받은 피해의 70%를 반사", "피격 시 2% 확률로 7초간 무적",
			"피격 시 4% 확률로 7초간 무적", "최종 데미지 증가", "숙련도",
			"공격 시 3% 확률로 53의 HP 회복", "공격 시 3% 확률로 53의 MP 회복",
			"공격 시 15% 확률로 95의 HP 회복", "공격 시 15% 확률로 95의 MP 회복",
			"공격 시 20% 확률로 200의 HP 회복", "공격 시 20% 확률로 110의 MP 회복",
			"공격 시 20% 확률로 300의 HP 회복", "공격 시 20% 확률로 165의 MP 회복",
			"공격 시 20% 확률로 360의 HP 회복", "공격 시 20% 확률로 180의 MP 회복",
			"공격 시 20% 확률로 5레벨 중독효과 적용", "공격 시 20% 확률로 6레벨 중독효과 적용",
			"공격 시 10% 확률로 2레벨 기절효과 적용",
			"공격 시 20% 확률로 2레벨 슬로우효과 적용",
			"공격 시 20% 확률로 2레벨 암흑효과 적용", "공격 시 20% 확률로 3레벨 암흑효과 적용",
			"공격 시 10% 확률로 2레벨 빙결효과 적용",
			"공격 시 10% 확률로 2레벨 봉인효과 적용",
			"공격 시 20% 확률로 240의 HP 회복", "공격 시 20% 확률로 120의 MP 회복",
			"공격 시 3% 확률로 47의 HP 회복", "공격 시 3% 확률로 47의 MP 회복",
			"공격 시 15% 확률로 85의 HP 회복", "공격 시 15% 확률로 85의 MP 회복",
			"몬스터 처치 시 15% 확률로 95의 HP 회복", "몬스터 처치 시 15% 확률로 95의 MP 회복",
			)

		fun potentialAbilityTextHelper(abilityCode: Int): SpannableStringBuilder {
			var string: String = abilityOptionTypeList[abilityCode%100]
			if(string.contains("피격 후 무적시간")) {
				return SpannableStringBuilder("${string}: +${abilityCode/100}초\n")
			} else if(string.contains("모든 스킬의 재사용 대기시간")) {
				return SpannableStringBuilder("${string}: -${abilityCode/100}초\n")
			} else if(string.contains("모든 스킬의 MP 소모")) {
				return SpannableStringBuilder("${string}: -${abilityCode/100}%\n")
			} else if(string.contains("쓸만한") || string.contains("피격 시") || string.contains("반사")
				|| (string.contains("공격 시") && !string.contains("보스 몬스터"))){
				return SpannableStringBuilder("${string}\n")
			} else if(string.contains("%")) {
				string = string.replace("%", "")
				return SpannableStringBuilder("${string}: +${abilityCode/100}%\n")
			} else {
				if(abilityCode/100 == 0) return SpannableStringBuilder("\n")
				return SpannableStringBuilder("${string}: +${abilityCode/100}\n")
			}
		} // 잠재능력 설명

		val characterCodeList = mapOf<String, Pair<List<Int>, String>>(
			"히어로" to Pair(listOf(1, 1, 1), "hero"),
			"팔라딘" to Pair(listOf(2, 1, 1), "paladin"),
			"다크나이트" to Pair(listOf(3, 1, 1), "darkknight"),
			"신궁" to Pair(listOf(4, 2, 1), "marksman"),
			"보우마스터" to Pair(listOf(5, 2, 1), "bowmaster"),
			"패스파인더" to Pair(listOf(6, 2, 1), "pathfinder"),
			"아크메이지(썬_콜)" to Pair(listOf(7, 3, 1), "archmageicelightning"),
			"아크메이지(불_독)" to Pair(listOf(8, 3, 1), "archmagefirepoison"),
			"비숍" to Pair(listOf(9, 3, 1), "bishop"),
			"나이트로드" to Pair(listOf(10, 4, 1), "nightlord"),
			"섀도어" to Pair(listOf(11, 4, 1), "shadower"),
			"듀얼블레이드" to Pair(listOf(12, 4, 1), "dualblade"),
			"바이퍼" to Pair(listOf(13, 5, 1), "viper"),
			"캡틴"  to Pair(listOf(14, 5, 1), "captain"),
			"캐논슈터" to Pair(listOf(15, 5, 1), "cannonshooter"),
			"소울마스터" to Pair(listOf(16, 1, 45), "soulmaster"),
			"윈드브레이커" to Pair(listOf(17, 2, 45), "windbreaker"),
			"플레임위자드" to Pair(listOf(18, 3, 45), "flamewizard"),
			"나이트워커" to Pair(listOf(19, 4, 45), "nightwalker"),
			"스트라이커" to Pair(listOf(20, 5, 45), "striker"),
			"아란" to Pair(listOf(21, 1, 3), "aran"),
			"메르세데스" to Pair(listOf(22, 2, 3), "mercedes"),
			"에반" to Pair(listOf(23, 3, 3), "evan"),
			"루미너스" to Pair(listOf(24, 3, 3), "luminous"),
			"팬텀" to Pair(listOf(25, 4, 3), "phantom"),
			"은월" to Pair(listOf(26, 5, 3), "shade"),
			"데몬어벤져" to Pair(listOf(27, 1, 4), ""),
			"데몬슬레이어" to Pair(listOf(28, 1, 4), ""),
			"블래스터" to Pair(listOf(29, 1, 4), "blaster"),
			"와일드헌터" to Pair(listOf(30, 2, 4), "wildhunter"),
			"배틀메이지" to Pair(listOf(31, 3, 4), "battlemage"),
			"제논" to Pair(listOf(32, 4, 4), "xenon"),
			"메카닉" to Pair(listOf(33, 5, 4), "mechanic"),
			"카이저" to Pair(listOf(34, 1, 5), ""),
			"카인" to Pair(listOf(35, 2, 5), ""),
			"카데나" to Pair(listOf(36, 4, 5), ""),
			"엔젤릭버스터" to Pair(listOf(37, 5, 5), ""),
			"아델" to Pair(listOf(38, 1, 38), "adele"),
			"일리움" to Pair(listOf(39, 3, 6), ""),
			"아크" to Pair(listOf(40, 5, 6), ""),
			"라라" to Pair(listOf(41, 3, 7), ""),
			"호영" to Pair(listOf(42, 4, 7), ""),
			"제로" to Pair(listOf(43, 1, 8), ""),
			"키네시스" to Pair(listOf(44, 3, 9), ""),
			"시그너스" to Pair(listOf(45, 6, 2), "")
			)

		val itemJob = mapOf<String, Pair<List<String>, Double>>(
			"제네시스 튜너" to Pair(listOf("아델"), 1.3),
			"제네시스 투핸드소드" to Pair(listOf("소울마스터", "히어로"), 1.34),
			"제네시스 투핸드해머" to Pair(listOf("팔라딘"), 1.34),
			"제네시스 폴암" to Pair(listOf("아란"), 1.49),
			"제네시스 엘라하" to Pair(listOf("블래스터"), 1.7),
			"제네시스 스피어" to Pair(listOf("다크나이트"), 1.49),
			"제네시스 보우" to Pair(listOf("윈드브레이커", "보우마스터"), 1.3),
			"제네시스 크로스보우" to Pair(listOf("신궁", "와일드헌터"), 1.35),
			"제네시스 에인션트 보우" to Pair(listOf("패스파인더"), 1.3),
			"제네시스 듀얼보우건" to Pair(listOf("메르세데스"), 1.3),
			"제네시스 스태프" to Pair(listOf("플레임위자드", "아크메이지(썬_콜)", "아크메이지(불_독)", "비숍", "에반", "배틀메이지"), 1.2),
			"제네시스 샤이닝로드" to Pair(listOf("루미너스"), 1.2),
			"제네시스 가즈" to Pair(listOf("나이트워커", "나이트로드"), 1.75),
			"제네시스 대거" to Pair(listOf("섀도어", "듀얼블레이드"), 1.3),
			"제네시스 케인" to Pair(listOf("팬텀"), 1.3),
			"제네시스 에너지체인" to Pair(listOf("제논"), 1.3125),
			"제네시스 클로" to Pair(listOf("스트라이커", "바이퍼", "은월"), 1.7),
			"제네시스 피스톨" to Pair(listOf("캡틴", "메카닉"), 1.5),
			"제네시스 시즈건" to Pair(listOf("캐논슈터"), 1.5)
		)

		fun setOptionMade(): MutableMap<Pair<Int, String>, Pair<List<Int>, List<List<Int>>>>{
			val setOption = emptyMap<Pair<Int, String>, Pair<List<Int>, List<List<Int>>>>().toMutableMap()
			var sol = MutableList(8){MutableList(100){0}} // setOptionList
			sol[2][8] = 30; sol[2][10] = 30; sol[2][19] = 10
			sol[3][8] = 30; sol[3][10] = 30; sol[3][17] = 10; sol[3][24] = 400
			sol[4][8] = 35; sol[4][10] = 35; sol[4][12] = 50; sol[4][19] = 10
			sol[5][8] = 40; sol[5][10] = 40; sol[5][19] = 10; sol[5][20] = 2000; sol[5][22] = 2000
			sol[6][8] = 30; sol[6][10] = 30; sol[6][21] = 30; sol[6][23] = 30
			sol[7][8] = 30; sol[7][10] = 30; sol[7][17] = 10
			setOption[Pair(102, "아케인셰이드 세트(전사)")] = Pair(listOf(0, 3, 6, 7, 8, 9, 10), sol)
			setOption[Pair(202, "아케인셰이드 세트(궁수)")] = Pair(listOf(0, 3, 6, 7, 8, 9, 10), sol)
			setOption[Pair(302, "아케인셰이드 세트(마법사)")] = Pair(listOf(0, 3, 6, 7, 8, 9, 10), sol)
			setOption[Pair(402, "아케인셰이드 세트(도적)")] = Pair(listOf(0, 3, 6, 7, 8, 9, 10), sol)
			setOption[Pair(502, "아케인셰이드 세트(해적)")] = Pair(listOf(0, 3, 6, 7, 8, 9, 10), sol)

			sol = MutableList(6){MutableList(100){0}}
			sol[2][8] = 40; sol[2][10] = 40; sol[2][19] = 10
			sol[3][8] = 40; sol[3][10] = 40; sol[3][12] = 50; sol[3][19] = 10
			sol[4][8] = 40; sol[4][10] = 40; sol[4][19] = 10
			sol[5][8] = 40; sol[5][10] = 40; sol[5][17] = 20
			setOption[Pair(103, "에테르넬 세트(전사)")] = Pair(listOf(0, 3, 4, 5, 10), sol)
			setOption[Pair(203, "에테르넬 세트(궁수)")] = Pair(listOf(0, 3, 4, 5, 10), sol)
			setOption[Pair(303, "에테르넬 세트(마법사)")] = Pair(listOf(0, 3, 4, 5, 10), sol)
			setOption[Pair(403, "에테르넬 세트(도적)")] = Pair(listOf(0, 3, 4, 5, 10), sol)
			setOption[Pair(503, "에테르넬 세트(해적)")] = Pair(listOf(0, 3, 4, 5, 10), sol)

			sol = MutableList(7){MutableList(100){0}}
			sol[2][8] = 20; sol[2][10] = 20; sol[2][19] = 10; sol[2][20] = 1500; sol[2][22] = 1500
			sol[3][8] = 20; sol[3][10] = 20; sol[3][12] = 30; sol[3][19] = 10
			sol[4][8] = 25; sol[4][10] = 25; sol[4][17] = 10; sol[4][24] = 200
			sol[5][8] = 30; sol[5][10] = 30; sol[5][19] = 10
			sol[6][8] = 20; sol[6][10] = 20; sol[6][21] = 20; sol[6][23] = 20
//			sol[7][8] = 20; sol[7][10] = 20; sol[7][17] = 10
			setOption[Pair(101, "앱솔랩스 세트(전사)")] = Pair(listOf(0, 3, 7, 8, 9, 10), sol)
			setOption[Pair(201, "앱솔랩스 세트(궁수)")] = Pair(listOf(0, 3, 7, 8, 9, 10), sol)
			setOption[Pair(301, "앱솔랩스 세트(마법사)")] = Pair(listOf(0, 3, 7, 8, 9, 10), sol)
			setOption[Pair(401, "앱솔랩스 세트(도적)")] = Pair(listOf(0, 3, 7, 8, 9, 10), sol)
			setOption[Pair(501, "앱솔랩스 세트(해적)")] = Pair(listOf(0, 3, 7, 8, 9, 10), sol)

			sol = MutableList(7){MutableList(100){0}}
			sol[2][12] = 20; sol[2][20] = 1000; sol[2][22] = 1000
			sol[3][8] = 50; sol[3][10] = 50
			sol[4][19] = 30

			setOption[Pair(100, "루타비스 세트(전사)")] = Pair(listOf(0, 3, 4, 5), sol)
			setOption[Pair(200, "루타비스 세트(궁수)")] = Pair(listOf(0, 3, 4, 5), sol)
			setOption[Pair(300, "루타비스 세트(마법사)")] = Pair(listOf(0, 3, 4, 5), sol)
			setOption[Pair(400, "루타비스 세트(도적)")] = Pair(listOf(0, 3, 4, 5), sol)
			setOption[Pair(500, "루타비스 세트(해적)")] = Pair(listOf(0, 3, 4, 5), sol)

			sol = MutableList(10){MutableList(100){0}} // setOptionList
			sol[2][8] = 10; sol[2][10] = 10; sol[2][12] = 10; sol[2][19] = 10
			sol[3][8] = 10; sol[3][10] = 10; sol[3][12] = 10; sol[3][17] = 10
			sol[4][8] = 15; sol[4][10] = 15; sol[4][12] = 15; sol[4][34] = 5
			sol[5][8] = 15; sol[5][10] = 15; sol[5][12] = 15; sol[5][19] = 10
			sol[6][8] = 15; sol[6][10] = 15; sol[6][12] = 15; sol[6][17] = 10
			sol[7][8] = 15; sol[7][10] = 15; sol[7][12] = 15; sol[7][34] = 5
			sol[8][8] = 15; sol[8][10] = 15; sol[8][12] = 15; sol[8][19] = 10
			sol[9][8] = 15; sol[9][10] = 15; sol[9][12] = 15; sol[9][34] = 5
			setOption[Pair(604, "칠흑의 보스 세트")] = Pair(listOf(2, 11, 12, 13, 14, 15, 17, 18, 20), sol)

			sol = MutableList(3){MutableList(100){0}}
			sol[2][8] = 10; sol[2][10] = 10; sol[2][12] = 10; sol[2][19] = 10
			setOption[Pair(606, "여명의 보스 세트")] = Pair(listOf(17, 18), sol)

			sol = MutableList(3){MutableList(100){0}}
			sol[2][17] = 10;
			setOption[Pair(609, "칠요 세트")] = Pair(listOf(16, 20), sol)
			return setOption
		}
	}
}