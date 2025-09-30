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
import bitc.fullstack502.final_project_team1.network.dto.SurveyListItemDto
import bitc.fullstack502.final_project_team1.ui.BaseActivity

class SurveyListActivity : BaseActivity() {

    override fun bottomNavSelectedItemId() = R.id.nav_survey_list

    companion object {
        private const val TMAP_PKG_NEW = "com.skt.tmap.ku"
        private const val TMAP_PKG_OLD = "com.skt.skaf.l001mtm091"
        private const val PLAY_STORE_TMAP = "market://details?id=$TMAP_PKG_NEW"
        private const val REQ_LOC_FOR_TMAP = 1200
        private const val REQ_LOC_PERMISSION = 2000
    }

    // XML id와 정확히 맞추기
    private val listContainer by lazy { findViewById<LinearLayout>(R.id.listContainer) }
    private val spinner by lazy { findViewById<Spinner>(R.id.spinnerSort) }
    private val tvTotalCount by lazy { findViewById<TextView>(R.id.tvTotalCount) }
    private val emptyState by lazy { findViewById<LinearLayout>(R.id.emptyStateLayout) }

    private var assignedList: List<AssignedBuilding> = emptyList()
    private var myLat: Double? = null
    private var myLng: Double? = null

    // 날짜 포맷
    private val parsePatterns = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ISO_DATE_TIME,
        DateTimeFormatter.ISO_OFFSET_DATE_TIME
    )
    private val outFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

    private fun parseDateFlexible(dateStr: String?): LocalDateTime? {
        if (dateStr.isNullOrBlank()) return null
        for (fmt in parsePatterns) {
            try { return LocalDateTime.parse(dateStr, fmt) } catch (_: Exception) {}
        }
        return try {
            java.time.ZonedDateTime.parse(dateStr).toLocalDateTime()
        } catch (_: Exception) { null }
    }
    private fun formatAssignedAt(dateStr: String?): String =
        parseDateFlexible(dateStr)?.let(outFormatter::format) ?: "미정"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_list)
        initHeader(title = "조사예정")

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabBack)
            ?.setOnClickListener { finish() }

        // 정렬 스피너
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: android.view.View?, pos: Int, id: Long) {
                sortAndBind(pos)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        loadMyLocation()
        // ✅ 서버 호출 한 번만: 여기서는 refreshAssigned()만 호출
        refreshAssigned()
    }

    override fun onResume() {
        super.onResume()
        refreshAssigned()
    }

    /** 서버에서 배정 목록 새로고침 */
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
                    // 현재 스피너 선택 기준으로 정렬/갱신
                    sortAndBind(spinner.selectedItemPosition)
                }
                .onFailure {
                    Toast.makeText(this@SurveyListActivity, "목록 조회 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    // 실패 시에도 UI 초기화
                    assignedList = emptyList()
                    bindList(assignedList)
                }
        }
    }

    /** 정렬 후 바인딩 */
    private fun sortAndBind(sortType: Int) {
        val sorted = when (sortType) {
            0 -> assignedList.sortedByDescending { parseDateFlexible(it.assignedAt) } // 최신등록순
            1 -> assignedList.sortedBy { parseDateFlexible(it.assignedAt) }           // 과거순
            2 -> {
                if (myLat != null && myLng != null) {
                    assignedList.sortedBy {
                        if (it.latitude != null && it.longitude != null)
                            distance(myLat!!, myLng!!, it.latitude, it.longitude)
                        else Double.MAX_VALUE
                    }
                } else assignedList
            }
            else -> assignedList
        }
        bindList(sorted)
    }

    /** 리스트 바인딩 (LinearLayout에 직접 row 추가) + 개수/빈상태 갱신 */
    private fun bindList(list: List<AssignedBuilding>) {
        listContainer.removeAllViews()
        val inf = LayoutInflater.from(this)

        list.forEach { item ->
            val row = inf.inflate(R.layout.item_survey, listContainer, false)

            val addrText = item.lotAddress?.takeIf { it.isNotBlank() } ?: "주소 없음"
            row.findViewById<TextView>(R.id.tvAddress).text = addrText

            row.findViewById<TextView?>(R.id.tvAssignedAt)?.text =
                "배정일자: ${formatAssignedAt(item.assignedAt)}"

            // 상세
            row.setOnClickListener {
                BuildingInfoBottomSheet.newInstanceForNew(item.id).apply {
                    arguments?.putString(EXTRA_RETURN_TO, ReturnTo.SURVEY_LIST.name)
                }.show(supportFragmentManager, "buildingInfo")
            }

            // 지도
            row.findViewById<Button?>(R.id.btnMap)?.setOnClickListener {
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
            row.findViewById<Button?>(R.id.btnRoute)?.setOnClickListener {
                val lat = item.latitude
                val lng = item.longitude
                if (lat == null || lng == null) {
                    Toast.makeText(this, "좌표 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    startTmapRouteFromMyLocation(destLat = lat, destLng = lng, destName = addrText)
                }
            }

            listContainer.addView(row)
        }

        // ✅ 총 개수/빈 상태 갱신 여기서!
        tvTotalCount.text = "총 ${list.size}건"
        if (list.isEmpty()) {
            emptyState.visibility = android.view.View.VISIBLE
            listContainer.visibility = android.view.View.GONE
        } else {
            emptyState.visibility = android.view.View.GONE
            listContainer.visibility = android.view.View.VISIBLE
        }
    }

    /** 위치 불러오기 (거리순 정렬 지원) */
    private fun loadMyLocation() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQ_LOC_PERMISSION
            )
            return
        }

        val fused = LocationServices.getFusedLocationProviderClient(this)

        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                myLat = loc.latitude
                myLng = loc.longitude
                sortAndBind(spinner.selectedItemPosition)
            } else {
                val cts = CancellationTokenSource()
                fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { cur ->
                        if (cur != null) {
                            myLat = cur.latitude
                            myLng = cur.longitude
                            sortAndBind(spinner.selectedItemPosition)
                        } else {
                            Toast.makeText(this, "내 위치를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "내 위치 확인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "내 위치 확인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /** 권한 결과 */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOC_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadMyLocation()
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
