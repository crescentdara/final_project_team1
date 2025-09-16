package bitc.fullstack502.final_project_team1.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.MapBottomSheetFragment
import bitc.fullstack502.final_project_team1.R
import java.net.URLEncoder

class SurveyListActivity : AppCompatActivity() {

    companion object {
        private const val TMAP_PKG_NEW = "com.skt.tmap.ku"
        private const val TMAP_PKG_OLD = "com.skt.skaf.l001mtm091"
        private const val PLAY_STORE_TMAP = "market://details?id=$TMAP_PKG_NEW"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_list)

        val btnMap = findViewById<Button>(R.id.btnMap)
        val btnRoute = findViewById<Button>(R.id.btnRoute)
        val spinnerSort = findViewById<Spinner>(R.id.spinnerSort)

        // 지도보기 버튼 → 지도 다이얼로그
        btnMap.setOnClickListener {
            MapBottomSheetFragment().show(supportFragmentManager, "mapDialog")
        }

        // 길찾기 버튼 → T맵 길찾기 실행 (출발/도착 자동입력)
        btnRoute.setOnClickListener {
            // ★ 여기에 실제 출발/도착 좌표 및 명칭을 넣으세요
            // 예시: 서울시청 → 남산타워
            openTmapRoute(
                startLat = 37.5665, startLng = 126.9780, startName = "서울시청",
                destLat  = 37.5512, destLng  = 126.9882,  destName  = "남산타워"
            )

            // ※ 현재 위치를 출발지로 쓰고 싶으면 start* 파라미터 빼고 goal*만 넘겨도 됩니다:
            // openTmapGoalOnly(destLat = 37.5512, destLng = 126.9882, destName = "남산타워")
        }
    }

    // ─── T맵 실행 유틸 ──────────────────────────────────────────────────────────────

    private fun isInstalled(pkg: String): Boolean = runCatching {
        packageManager.getPackageInfo(pkg, 0); true
    }.getOrDefault(false)

    /** 출발/도착 모두 지정해서 T맵 길찾기 열기 (x=경도, y=위도 주의) */
    private fun openTmapRoute(
        startLat: Double, startLng: Double, startName: String,
        destLat: Double,  destLng: Double,  destName: String
    ) {
        val sName = URLEncoder.encode(startName, "UTF-8")
        val dName = URLEncoder.encode(destName, "UTF-8")

        val uri = Uri.parse(
            "tmap://route" +
                    "?startx=$startLng&starty=$startLat&startname=$sName" +
                    "&goalx=$destLng&goaly=$destLat&goalname=$dName"
        )

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            `package` = when {
                isInstalled(TMAP_PKG_NEW) -> TMAP_PKG_NEW
                isInstalled(TMAP_PKG_OLD) -> TMAP_PKG_OLD
                else -> null
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (intent.`package` == null) {
            // T맵 미설치 → 플레이스토어 열기 (또는 구글지도 네비로 대체)
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_TMAP)))
            } catch (_: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$destLat,$destLng")))
            }
        } else {
            startActivity(intent)
        }
    }

    /** 출발지 없이 목적지만 넘기는 간단 버전 (T맵이 현재 위치를 출발지로 사용) */
    private fun openTmapGoalOnly(destLat: Double, destLng: Double, destName: String) {
        val dName = URLEncoder.encode(destName, "UTF-8")
        val uri = Uri.parse("tmap://route?goalx=$destLng&goaly=$destLat&goalname=$dName")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            `package` = if (isInstalled(TMAP_PKG_NEW)) TMAP_PKG_NEW
            else if (isInstalled(TMAP_PKG_OLD)) TMAP_PKG_OLD else null
        }
        if (intent.`package` == null) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_TMAP)))
        } else {
            startActivity(intent)
        }
    }
}
