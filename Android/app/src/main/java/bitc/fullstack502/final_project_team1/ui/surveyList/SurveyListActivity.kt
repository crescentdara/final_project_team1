package bitc.fullstack502.final_project_team1.ui.surveyList

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.final_project_team1.MainActivity
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.AssignedBuilding
import bitc.fullstack502.final_project_team1.ui.adapter.SurveyAdapter
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.transmission.DataTransmissionActivity
import bitc.fullstack502.final_project_team1.ui.transmission.TransmissionCompleteActivity
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SurveyListActivity : AppCompatActivity() {

    companion object {
        private const val TMAP_PKG_NEW = "com.skt.tmap.ku"
        private const val TMAP_PKG_OLD = "com.skt.skaf.l001mtm091"
        private const val PLAY_STORE_TMAP = "market://details?id=$TMAP_PKG_NEW"
        private const val REQ_LOC_FOR_TMAP = 1200
        private const val REQ_LOC_PERMISSION = 2000
    }

    private val spinner by lazy { findViewById<Spinner>(R.id.spinnerSort) }
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SurveyAdapter

    private var assignedList: List<AssignedBuilding> = emptyList()
    private var myLat: Double? = null
    private var myLng: Double? = null

    // 서버에서 내려오는 assignedAt 파싱용 포맷터 (ISO-8601 가정)
    private val formatter = DateTimeFormatter.ISO_DATE_TIME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_list)

        // UI 초기화
        setupToolbar()
        setupFloatingButton()

        // RecyclerView 세팅
        recyclerView = findViewById(R.id.recyclerSurveyList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SurveyAdapter(emptyList())
        recyclerView.adapter = adapter

        // 내 위치 (거리순 정렬용)
        loadMyLocation()

        // 리스트 채우기
        CoroutineScope(Dispatchers.Main).launch {
            val uid = AuthManager.userId(this@SurveyListActivity)
            if (uid <= 0) {
                Toast.makeText(this@SurveyListActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            runCatching { ApiClient.service.getAssigned(uid) }
                .onSuccess { list ->
                    assignedList = list
                    adapter.updateData(assignedList)   // ✅ 그대로 전달

                    // 스피너 리스너 연결
                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            sortAndBind(position)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                }
                .onFailure {
                    Toast.makeText(
                        this@SurveyListActivity,
                        "목록 조회 실패: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    /** ───── 위치 불러오기 ───── */
    private fun loadMyLocation() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

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
        fused.lastLocation.addOnSuccessListener { loc ->
            myLat = loc?.latitude
            myLng = loc?.longitude
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

    /** 문자열 → LocalDateTime 변환 */
    private fun parseDate(dateStr: String?): LocalDateTime? {
        return try {
            if (dateStr != null) LocalDateTime.parse(dateStr, formatter) else null
        } catch (e: Exception) {
            null
        }
    }

    /** 정렬 */
    private fun sortAndBind(sortType: Int) {
        val sorted = when (sortType) {
            0 -> assignedList.sortedByDescending { parseDate(it.assignedAt?.toString()) } // 최신등록순
            1 -> assignedList.sortedBy { parseDate(it.assignedAt?.toString()) }           // 과거순
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
        adapter.updateData(sorted)
    }

    /** 거리 계산 (단위 m) */
    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3
        val φ1 = Math.toRadians(lat1)
        val φ2 = Math.toRadians(lat2)
        val Δφ = Math.toRadians(lat2 - lat1)
        val Δλ = Math.toRadians(lon2 - lon1)

        val a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                Math.cos(φ1) * Math.cos(φ2) *
                Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    // ─── 길찾기(T맵) ─────────────────────────────────────────────
    private fun startTmapRouteFromMyLocation(destLat: Double, destLng: Double, destName: String) {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
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
        val uri =
            Uri.parse("tmap://route?startx=$startLng&starty=$startLat&startname=$sName&goalx=$destLng&goaly=$destLat&goalname=$dName")
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
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=$destLat,$destLng")
                    )
                )
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

    /**
     * 🔧 상단 툴바 설정
     */
    private fun setupToolbar() {
        findViewById<ImageView>(R.id.ivHamburger)?.setOnClickListener { view ->
            showCategoryPopup(view)
        }
        findViewById<ImageView>(R.id.ivLogo)?.setOnClickListener {
            navigateToMain()
        }
        findViewById<TextView>(R.id.tvLogout)?.setOnClickListener {
            performLogout()
        }
    }

    /**
     * 🎈 플로팅 뒤로가기 버튼 설정
     */
    private fun setupFloatingButton() {
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabBack)?.setOnClickListener {
            onBackPressed()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun performLogout() {
        AuthManager.clear(this)
        Toast.makeText(this, "로그아웃 완료", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun showCategoryPopup(anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.modal_category, null)
        val displayMetrics = resources.displayMetrics
        val popupWidth = (displayMetrics.widthPixels * 0.6).toInt()
        val popupHeight = resources.getDimensionPixelSize(R.dimen.category_popup_height)
        val popup = PopupWindow(popupView, popupWidth, popupHeight, true)
        setupPopupMenuItems(popupView, popup)
        popup.showAsDropDown(anchor, 0, 0, Gravity.START)
    }

    private fun setupPopupMenuItems(popupView: View, popup: PopupWindow) {
        popupView.findViewById<ImageView>(R.id.btnClose)?.setOnClickListener { popup.dismiss() }
        popupView.findViewById<MaterialButton>(R.id.btnSurveyScheduled)
            ?.setOnClickListener { popup.dismiss() }
        popupView.findViewById<MaterialButton>(R.id.btnResurveyTarget)
            ?.setOnClickListener { popup.dismiss() }
        popupView.findViewById<MaterialButton>(R.id.btnSurveyHistory)?.setOnClickListener {
            startActivity(Intent(this, TransmissionCompleteActivity::class.java))
            popup.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnNotTransmitted)?.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java))
            popup.dismiss()
        }
    }
}