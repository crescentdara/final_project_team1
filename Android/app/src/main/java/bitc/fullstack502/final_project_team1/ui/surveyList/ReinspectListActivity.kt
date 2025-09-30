package bitc.fullstack502.final_project_team1.ui.surveyList

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.SurveyListItemDto
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import android.view.View
import bitc.fullstack502.final_project_team1.network.dto.ReturnTo
import bitc.fullstack502.final_project_team1.network.dto.EXTRA_RETURN_TO
import bitc.fullstack502.final_project_team1.ui.BaseActivity
import com.google.android.material.appbar.MaterialToolbar
import android.widget.TextView

class ReinspectListActivity : BaseActivity() {

    override fun bottomNavSelectedItemId() = R.id.nav_reinspect

    companion object {
        private const val REQ_LOC_FOR_TMAP = 9120
        private const val REQ_LOC_PERMISSION = 9121

        private const val TMAP_PKG_NEW = "com.skt.tmap.ku"
        private const val TMAP_PKG_OLD = "com.skt.skaf.l001mtm091"
        private const val PLAY_STORE_TMAP = "market://details?id=$TMAP_PKG_NEW"
    }

    private lateinit var spinnerSort: Spinner
    private lateinit var listContainer: LinearLayout

    // ───── 날짜 포맷 유틸 (SurveyListActivity와 동일 규칙) ─────
    private val parsePatterns = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ISO_DATE_TIME,
        DateTimeFormatter.ISO_OFFSET_DATE_TIME
    )
    private val outFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

    private fun updateHeaderCount(count: Int) {
        val title = "재조사"

        val ab = supportActionBar
        if (ab != null) {
            ab.title = title
        } else {
            findViewById<MaterialToolbar>(R.id.toolbar)?.title = title
        }

        // 헤더의 "총 N건" 텍스트도 함께 갱신 (뷰 없으면 무시)
        findViewById<TextView>(R.id.tvTotalCount)?.text = "총 ${count}건"
    }

    // 2) 날짜 파싱 (named args 제거)
    private fun parseDateFlexible(dateStr: String?): LocalDateTime? {
        if (dateStr.isNullOrBlank()) return null
        for (fmt in parsePatterns) {
            try {
                return LocalDateTime.parse(dateStr, fmt)
            } catch (_: Exception) { /* 다음 포맷 시도 */ }
        }
        return try {
            ZonedDateTime.parse(dateStr).toLocalDateTime()
        } catch (_: Exception) {
            null
        }
    }

    // 3) 날짜 포맷 (named args 제거)
    private fun formatAssignedAt(dateStr: String?): String {
        val dt = parseDateFlexible(dateStr) ?: return "미정"
        return outFormatter.format(dt)
    }
    // ──────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_list)
        initHeader(title = "재조사")

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabBack)
            ?.setOnClickListener { finish() }

        spinnerSort = findViewById(R.id.spinnerSort)
        listContainer = findViewById(R.id.listContainer)

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                loadAndRender()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 좌표 프리로드 필요 없음(서버가 item별 latitude/longitude를 내려줌)
        loadAndRender()
    }


    private fun loadAndRender() {
        lifecycleScope.launch {
            try {
                val uid = AuthManager.userId(this@ReinspectListActivity)
                if (uid <= 0) {
                    Toast.makeText(this@ReinspectListActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val res = ApiClient.service.getSurveysReJe(
                    userId = uid,
                    status = "REJECTED",
                    page = 0,
                    size = 200
                )

                var items = res.page.content
                    .filter { it.status.equals("REJECTED", ignoreCase = true) }

                when (spinnerSort.selectedItemPosition) {
                    1 -> items = items.sortedBy { it.address ?: "" }                 // 주소순
                    2 -> items = items.sortedByDescending { it.assignedAtIso ?: "" } // 배정일자순(최신)
                    else -> { /* 기본 정렬 유지 */ }
                }

                updateHeaderCount(items.size)
                render(items)

            } catch (e: Exception) {
                Toast.makeText(this@ReinspectListActivity, "재조사 목록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun render(list: List<SurveyListItemDto>) {
        listContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        list.forEach { item ->
            val row = inflater.inflate(R.layout.item_survey, listContainer, false)

            val tvAddress    = row.findViewById<TextView>(R.id.tvAddress)
            val tvAssignedAt = row.findViewById<TextView?>(R.id.tvAssignedAt)
            val btnMap       = row.findViewById<Button>(R.id.btnMap)
            val btnRoute     = row.findViewById<Button>(R.id.btnRoute)

            // ✅ 배지
            row.findViewById<TextView?>(R.id.tvSurveyBadge)?.apply {
                text = "반려"
            }

            // 재조사 목록에서는 배정일자 숨김
            tvAssignedAt?.visibility = View.GONE

            val addrText = item.address ?: (item.buildingName ?: "건물 #${item.buildingId}")
            tvAddress.text = addrText


            // ✅ 지도: 좌표 있으면 네이버맵 모달, 없으면 주소검색 폴백
            btnMap.setOnClickListener {
                val lat = item.latitude
                val lng = item.longitude
                if (lat != null && lng != null) {
                    MapBottomSheetFragment.newInstance(lat, lng, addrText)
                        .show(supportFragmentManager, "mapDialog")
                } else {
                    val q = URLEncoder.encode(addrText, "UTF-8")
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$q")))
                }
            }

            // ✅ 길찾기(T map): 좌표 있으면 route, 없으면 goalname만
            btnRoute.setOnClickListener {
                val lat = item.latitude
                val lng = item.longitude
                if (lat != null && lng != null) {
                    startTmapRouteFromMyLocation(lat, lng, addrText)
                } else {
                    val dName = URLEncoder.encode(addrText, "UTF-8")
                    val intent = tmapIntent(Uri.parse("tmap://route?goalname=$dName"))
                    if (intent.`package` == null)
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_TMAP)))
                    else startActivity(intent)
                }
            }

            // 카드 클릭 → 기존 바텀시트
            row.setOnClickListener {
                BuildingInfoBottomSheet.newInstanceForReinspect(
                    surveyId     = item.surveyId,
                    buildingId   = item.buildingId,
                    address      = item.address,
                    buildingName = item.buildingName,
                    rejectReason = item.rejectReason,
                    rejectedAt   = item.assignedAtIso
                ).apply {
                    arguments?.putString(EXTRA_RETURN_TO, ReturnTo.REINSPECT.name)
                }.show(supportFragmentManager, "building_info")
            }

            listContainer.addView(row)
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
                val cts = CancellationTokenSource()
                fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { cur ->
                        if (cur != null) {
                            openTmapRoute(
                                startLat = cur.latitude, startLng = cur.longitude, startName = "현재위치",
                                destLat = destLat, destLng = destLng, destName = destName
                            )
                        } else {
                            openTmapGoalOnly(destLat, destLng, destName)
                        }
                    }
                    .addOnFailureListener {
                        openTmapGoalOnly(destLat, destLng, destName)
                    }
            }
        }.addOnFailureListener {
            openTmapGoalOnly(destLat, destLng, destName)
        }
    }

    private fun isInstalled(pkg: String) = runCatching {
        packageManager.getPackageInfo(pkg, 0); true
    }.getOrDefault(false)

    private fun tmapIntent(uri: Uri): Intent =
        Intent(Intent.ACTION_VIEW, uri).apply {
            `package` = when {
                isInstalled(TMAP_PKG_NEW) -> TMAP_PKG_NEW
                isInstalled(TMAP_PKG_OLD) -> TMAP_PKG_OLD
                else -> null
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    override fun onResume() {
        super.onResume()
        loadAndRender()   // ★ 복귀마다 강제 리프레시
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent != null) setIntent(intent) // FLAG_ACTIVITY_CLEAR_TOP|SINGLE_TOP 대비
        loadAndRender()   // ★ CLEAR_TOP 재진입에서도 리프레시
    }


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
                val onlyGoal = tmapIntent(Uri.parse("tmap://route?goalx=$destLng&goaly=$destLat&goalname=$dName"))
                if (onlyGoal.`package` == null)
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_TMAP)))
                else startActivity(onlyGoal)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOC_PERMISSION &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 필요 시 재시도 포인트
        }
        if (requestCode == REQ_LOC_FOR_TMAP &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "다시 길찾기를 눌러주세요.", Toast.LENGTH_SHORT).show()
        }

    }
}
