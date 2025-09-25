package bitc.fullstack502.final_project_team1.ui.surveyList

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.util.Log
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import android.location.Location
import bitc.fullstack502.final_project_team1.network.dto.ReturnTo
import bitc.fullstack502.final_project_team1.network.dto.EXTRA_RETURN_TO

class SurveyListActivity : AppCompatActivity() {

    companion object {
        private const val TMAP_PKG_NEW = "com.skt.tmap.ku"
        private const val TMAP_PKG_OLD = "com.skt.skaf.l001mtm091"
        private const val PLAY_STORE_TMAP = "market://details?id=$TMAP_PKG_NEW"
        private const val REQ_LOC_FOR_TMAP = 1200
        private const val REQ_LOC_PERMISSION = 2000
    }

    private val container by lazy { findViewById<LinearLayout>(R.id.listContainer) }
    private val spinner by lazy { findViewById<Spinner>(R.id.spinnerSort) }

    private var assignedList: List<AssignedBuilding> = emptyList()
    private var myLat: Double? = null
    private var myLng: Double? = null

    // 여러 날짜 포맷 지원 (DB 스샷 포함)
    private val parsePatterns = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"), // 2025-09-22 09:36:26
        DateTimeFormatter.ISO_DATE_TIME,                    // 2025-09-22T09:36:26 (or millis)
        DateTimeFormatter.ISO_OFFSET_DATE_TIME              // 2025-09-22T09:36:26+09:00
    )
    private val outFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

    private fun parseDateFlexible(dateStr: String?): LocalDateTime? {
        if (dateStr.isNullOrBlank()) return null
        for (fmt in parsePatterns) {
            try { return LocalDateTime.parse(dateStr, fmt) } catch (_: Exception) {}
        }
        // 타임존 포함 문자열 대응
        return try {
            val zdt = java.time.ZonedDateTime.parse(dateStr)
            zdt.toLocalDateTime()
        } catch (_: Exception) {
            null
        }
    }


    private fun formatAssignedAt(dateStr: String?): String {
        val dt = parseDateFlexible(dateStr) ?: return "미정"
        return outFormatter.format(dt)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_list)

        // 정렬 스피너 리스너
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: android.view.View?, pos: Int, id: Long) {
                sortAndBind(pos)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 내 위치 (거리순 정렬용)
        loadMyLocation()
        refreshAssigned()

        // 목록 불러오기
        CoroutineScope(Dispatchers.Main).launch {
            val uid = AuthManager.userId(this@SurveyListActivity)
            if (uid <= 0) {
                Toast.makeText(this@SurveyListActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            runCatching { ApiClient.service.getAssigned(uid) }
                .onSuccess { list ->
                    assignedList = list
                    bindList(assignedList)                    // 1차 표시
                    sortAndBind(spinner.selectedItemPosition) // 현재 선택 기준으로 재정렬
                }
                .onFailure {
                    Toast.makeText(this@SurveyListActivity, "목록 조회 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // ▼ 추가
    override fun onResume() {
        super.onResume()
        refreshAssigned()
    }



    // ▼ 추가
    private fun refreshAssigned() {
        CoroutineScope(Dispatchers.Main).launch {
            val uid = AuthManager.userId(this@SurveyListActivity)
            if (uid <= 0) {
                Toast.makeText(this@SurveyListActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            runCatching { ApiClient.service.getAssigned(uid) }
                .onSuccess { list ->
                    assignedList = list
                    bindList(assignedList)                    // 1차 표시
                    sortAndBind(spinner.selectedItemPosition) // 현재 선택 기준으로 재정렬
                }
                .onFailure {
                    Toast.makeText(this@SurveyListActivity, "목록 조회 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    /** ───── 위치 불러오기 ───── */
    private fun loadMyLocation() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQ_LOC_PERMISSION
            )
            return
        }

        val fused = LocationServices.getFusedLocationProviderClient(this)

        // 1차: 빠른 lastLocation
        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                myLat = loc.latitude
                myLng = loc.longitude
                Log.d("SurveyList", "lastLocation lat=$myLat, lng=$myLng")
                sortAndBind(spinner.selectedItemPosition)   // ✅ 위치 들어오는 즉시 재정렬
            } else {
                // 2차: 한 번만 현재 위치 요청
                val cts = CancellationTokenSource()
                fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { cur ->
                        if (cur != null) {
                            myLat = cur.latitude
                            myLng = cur.longitude
                            Log.d("SurveyList", "getCurrentLocation lat=$myLat, lng=$myLng")
                            sortAndBind(spinner.selectedItemPosition)
                        } else {
                            Log.w("SurveyList", "getCurrentLocation returned null")
                            Toast.makeText(this, "내 위치를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("SurveyList", "getCurrentLocation failed: ${e.message}")
                        Toast.makeText(this, "내 위치 확인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("SurveyList", "lastLocation failed: ${e.message}")
            Toast.makeText(this, "내 위치 확인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    /** 권한 요청 결과 처리 */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOC_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            loadMyLocation()
        }
    }

    /** 정렬 + 바인딩 */
    private fun sortAndBind(sortType: Int) {
        val sorted = when (sortType) {
            0 -> assignedList.sortedByDescending { parseDateFlexible(it.assignedAt) } // 최신등록순
            1 -> assignedList.sortedBy { parseDateFlexible(it.assignedAt) }           // 과거순
            2 -> {
                if (myLat != null && myLng != null) {
                    assignedList.sortedBy {
                        if (it.latitude != null && it.longitude != null) {
                            distance(myLat!!, myLng!!, it.latitude, it.longitude)
                        } else Double.MAX_VALUE
                    }
                } else assignedList
            }
            else -> assignedList
        }
        bindList(sorted)
    }

    /** 리스트 바인딩 (LinearLayout에 직접 추가) */
    private fun bindList(list: List<AssignedBuilding>) {
        container.removeAllViews()
        val inf = LayoutInflater.from(this)

        list.forEach { item ->
            val row = inf.inflate(R.layout.item_survey, container, false)

            val addrText = item.lotAddress?.takeIf { it.isNotBlank() } ?: "주소 없음"
            row.findViewById<TextView>(R.id.tvAddress).text = addrText

            // 배정일시 표시
            row.findViewById<TextView?>(R.id.tvAssignedAt)?.text =
                "배정일자: ${formatAssignedAt(item.assignedAt)}"

            // 카드 클릭 → 건물 상세
            row.setOnClickListener {
                BuildingInfoBottomSheet.newInstanceForNew(item.id).apply {
                    arguments?.putString(EXTRA_RETURN_TO, ReturnTo.SURVEY_LIST.name)
                }.show(supportFragmentManager, "buildingInfo")
            }

            // 지도보기
            row.findViewById<Button>(R.id.btnMap).setOnClickListener {
                val lat = item.latitude
                val lng = item.longitude
                if (lat == null || lng == null) {
                    Toast.makeText(this, "좌표 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    MapBottomSheetFragment.newInstance(lat, lng, addrText)
                        .show(supportFragmentManager, "mapDialog")
                }
            }

            // 길찾기
            row.findViewById<Button>(R.id.btnRoute).setOnClickListener {
                val lat = item.latitude
                val lng = item.longitude
                if (lat == null || lng == null) {
                    Toast.makeText(this, "좌표 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    startTmapRouteFromMyLocation(
                        destLat = lat,
                        destLng = lng,
                        destName = addrText
                    )
                }
            }

            container.addView(row)
        }
    }

    /** 거리 계산 (단위 m) */
    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val out = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, out)
        return out[0].toDouble()
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
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_TMAP)))
            } catch (_: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$destLat,$destLng")))
            }
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
        if (intent.`package` == null)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_TMAP)))
        else startActivity(intent)
    }

    fun refreshAssignments() {
        CoroutineScope(Dispatchers.Main).launch {
            val uid = AuthManager.userId(this@SurveyListActivity)
            if (uid <= 0) return@launch

            runCatching { ApiClient.service.getAssigned(uid) }
                .onSuccess { list ->
                    assignedList = list
                    sortAndBind(spinner.selectedItemPosition)
                }
                .onFailure {
                    Toast.makeText(this@SurveyListActivity, "목록 재조회 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

}
