// ui/surveyList/SurveyListActivity.kt
package bitc.fullstack502.final_project_team1.ui.surveyList

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.AssignedBuilding
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder

class SurveyListActivity : AppCompatActivity() {

    companion object {
        private const val TMAP_PKG_NEW = "com.skt.tmap.ku"
        private const val TMAP_PKG_OLD = "com.skt.skaf.l001mtm091"
        private const val PLAY_STORE_TMAP = "market://details?id=$TMAP_PKG_NEW"
        private const val REQ_LOC_FOR_TMAP = 1200
    }

    private val container by lazy { findViewById<LinearLayout>(R.id.listContainer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_list)

        // 리스트 채우기
        CoroutineScope(Dispatchers.Main).launch {
            val uid = AuthManager.userId(this@SurveyListActivity)
            if (uid <= 0) {
                Toast.makeText(this@SurveyListActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                // TODO: startActivity(Intent(this@SurveyListActivity, LoginActivity::class.java)); finish()
                return@launch
            }

            runCatching { ApiClient.service.getAssigned(uid) }
                .onSuccess { list -> bindList(list) }
                .onFailure {
                    Toast.makeText(this@SurveyListActivity, "목록 조회 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun bindList(list: List<AssignedBuilding>) {
        container.removeAllViews()
        val inf = LayoutInflater.from(this)
        list.forEach { item ->
            val row = inf.inflate(R.layout.item_survey, container, false)

            row.findViewById<android.widget.TextView>(R.id.tvAddress).text = item.lotAddress

            // 지도보기 → 바텀시트(마커 표시)
            row.findViewById<android.widget.Button>(R.id.btnMap).setOnClickListener {
                val lat = item.latitude
                val lng = item.longitude
                if (lat == null || lng == null) {
                    Toast.makeText(this, "좌표 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    MapBottomSheetFragment.newInstance(lat, lng, item.lotAddress)
                        .show(supportFragmentManager, "mapDialog")
                }
            }

            // 길찾기 → T맵
            row.findViewById<android.widget.Button>(R.id.btnRoute).setOnClickListener {
                val lat = item.latitude
                val lng = item.longitude
                if (lat == null || lng == null) {
                    Toast.makeText(this, "좌표 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    startTmapRouteFromMyLocation(destLat = lat, destLng = lng, destName = item.lotAddress)
                }
            }

            container.addView(row)
        }
    }

    // ─── 길찾기(T맵) ─────────────────────────────────────────────
    private fun startTmapRouteFromMyLocation(destLat: Double, destLng: Double, destName: String) {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQ_LOC_FOR_TMAP
            )
            // 권한 응답 후 재시도 로직이 필요하면 목적지 임시 저장 멤버 추가하세요.
            return
        }

        val fused = LocationServices.getFusedLocationProviderClient(this)
        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                openTmapRoute(
                    startLat = loc.latitude, startLng = loc.longitude, startName = "현재위치",
                    destLat = destLat, destLng = destLng, destName = destName
                )
            } else {
                openTmapGoalOnly(destLat, destLng, destName)
            }
        }.addOnFailureListener {
            openTmapGoalOnly(destLat, destLng, destName)
        }
    }

    private fun isInstalled(pkg: String) = runCatching {
        packageManager.getPackageInfo(pkg, 0); true
    }.getOrDefault(false)

    private fun openTmapRoute(
        startLat: Double, startLng: Double, startName: String,
        destLat: Double, destLng: Double, destName: String
    ) {
        val sName = URLEncoder.encode(startName, "UTF-8")
        val dName = URLEncoder.encode(destName, "UTF-8")
        val uri = Uri.parse("tmap://route?startx=$startLng&starty=$startLat&startname=$sName&goalx=$destLng&goaly=$destLat&goalname=$dName")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            `package` = when {
                isInstalled(TMAP_PKG_NEW) -> TMAP_PKG_NEW
                isInstalled(TMAP_PKG_OLD) -> TMAP_PKG_OLD
                else -> null
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.`package` == null) {
            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_TMAP))) }
            catch (_: Exception) { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$destLat,$destLng"))) }
        } else startActivity(intent)
    }

    private fun openTmapGoalOnly(destLat: Double, destLng: Double, destName: String) {
        val dName = URLEncoder.encode(destName, "UTF-8")
        val uri = Uri.parse("tmap://route?goalx=$destLng&goaly=$destLat&goalname=$dName")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            `package` = when {
                isInstalled(TMAP_PKG_NEW) -> TMAP_PKG_NEW
                isInstalled(TMAP_PKG_OLD) -> TMAP_PKG_OLD
                else -> null
            }
        }
        if (intent.`package` == null) startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_TMAP)))
        else startActivity(intent)
    }
}
