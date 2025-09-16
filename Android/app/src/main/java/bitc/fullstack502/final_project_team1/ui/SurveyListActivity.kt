package bitc.fullstack502.final_project_team1.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import bitc.fullstack502.final_project_team1.R
import com.google.android.gms.location.LocationServices
import java.net.URLEncoder

class SurveyListActivity : AppCompatActivity() {

    companion object {
        private const val TMAP_PKG_NEW = "com.skt.tmap.ku"
        private const val TMAP_PKG_OLD = "com.skt.skaf.l001mtm091"
        private const val PLAY_STORE_TMAP = "market://details?id=$TMAP_PKG_NEW"
        private const val REQ_LOC_FOR_TMAP = 1200
    }

    // ★ 데모용 목적지(남산타워)
    private val destLat = 37.5512
    private val destLng = 126.9882
    private val destName = "남산타워"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_list)

        val btnMap = findViewById<Button>(R.id.btnMap)
        val btnRoute = findViewById<Button>(R.id.btnRoute)
        val spinnerSort = findViewById<Spinner>(R.id.spinnerSort)

        btnMap.setOnClickListener {
            MapBottomSheetFragment().show(supportFragmentManager, "mapDialog")
        }

        btnRoute.setOnClickListener {
            startTmapRouteFromMyLocation()
        }
    }

    private fun startTmapRouteFromMyLocation() {
        // 권한 체크
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQ_LOC_FOR_TMAP
            )
            return
        }

        // 마지막 위치 얻기
        val fused = LocationServices.getFusedLocationProviderClient(this)
        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                openTmapRoute(
                    startLat = loc.latitude,
                    startLng = loc.longitude,
                    startName = "현재위치",
                    destLat = destLat,
                    destLng = destLng,
                    destName = destName
                )
            } else {
                // 위치가 없으면 목적지만 전달
                openTmapGoalOnly(destLat, destLng, destName)
            }
        }.addOnFailureListener {
            openTmapGoalOnly(destLat, destLng, destName)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOC_FOR_TMAP) {
            // 허용되면 다시 시도
            val granted = grantResults.any { it == PackageManager.PERMISSION_GRANTED }
            if (granted) startTmapRouteFromMyLocation()
            else openTmapGoalOnly(destLat, destLng, destName)
        }
    }

    // ─── T맵 실행 유틸 ──────────────────────────────────────────────────────────────

    private fun isInstalled(pkg: String): Boolean = runCatching {
        packageManager.getPackageInfo(pkg, 0); true
    }.getOrDefault(false)

    /** 출발/도착 모두 지정 (x=경도, y=위도) */
    private fun openTmapRoute(
        startLat: Double, startLng: Double, startName: String,
        destLat: Double,  destLng: Double,  destName: String
    ) {
        val sName = URLEncoder.encode(startName, "UTF-8")
        val dName = URLEncoder.encode(destName, "UTF-8")
        val uri = Uri.parse(
            "tmap://route?startx=$startLng&starty=$startLat&startname=$sName&goalx=$destLng&goaly=$destLat&goalname=$dName"
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
            // 미설치 → 스토어 or 구글지도 대체
            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_TMAP))) }
            catch (_: Exception) { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$destLat,$destLng"))) }
        } else {
            startActivity(intent)
        }
    }

    /** 출발지 없이 목적지만 넘기는 간단 버전 */
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
        if (intent.`package` == null) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_TMAP)))
        } else {
            startActivity(intent)
        }
    }
}
