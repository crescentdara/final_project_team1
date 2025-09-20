package bitc.fullstack502.final_project_team1.ui.transmission

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.final_project_team1.MainActivity
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.survey.SurveyResultDialog
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 📤 미전송(임시저장) 페이지
 * - 서버에서 status=TEMP 만 조회해서 표시
 * - 정렬: 최신순/과거순
 */
class DataTransmissionActivity : AppCompatActivity() {

    // UI
    private lateinit var spinnerSort: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var adapter: NotTransmittedListAdapter

    // 데이터 (주소만 표시)
    private val allDataList = mutableListOf<String>()
    private val sortedDataList = mutableListOf<String>()

    private val sortOptions = arrayOf("최신순", "과거순")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_transmission)

        initViews()
        setupToolbar()
        setupSort()
        setupRecyclerView()

        // ✅ 서버에서 임시저장(TEMP) 목록만 로드
        loadTempFromServer()
    }

    private fun initViews() {
        spinnerSort = findViewById(R.id.spinnerSort)
        recyclerView = findViewById(R.id.recyclerNotTransmittedList)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabBack)
            ?.setOnClickListener { onBackPressed() }
    }

    private fun setupToolbar() {
        findViewById<ImageView>(R.id.ivHamburger)?.setOnClickListener { view ->
            showCategoryPopup(view)
        }
        findViewById<ImageView>(R.id.ivLogo)?.setOnClickListener { navigateToMain() }
        findViewById<TextView>(R.id.tvLogout)?.setOnClickListener { performLogout() }
    }

    private fun setupSort() {
        val spinnerAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, sortOptions
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerSort.adapter = spinnerAdapter
        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applySorting(sortOptions[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                applySorting("최신순")
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = NotTransmittedListAdapter(sortedDataList) { address ->
            // 아이템 클릭 시: 미리보기/재전송 등 필요한 UX로 연결
            SurveyResultDialog(this, address) {
                // 전송 성공 시 전송완료 화면으로 이동하고 싶다면 여기서 처리
                // startActivity(Intent(this, TransmissionCompleteActivity::class.java))
                // finish()
            }.show()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    /** 🔄 서버에서 TEMP만 로드 */
    private fun loadTempFromServer() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val uid = AuthManager.userId(this@DataTransmissionActivity)
                if (uid <= 0) {
                    Toast.makeText(this@DataTransmissionActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val resp = ApiClient.service.getSurveys(
                    userId = uid,
                    status = "TEMP",   // ⬅️ 임시저장만!
                    page = 0,
                    size = 50
                )

                // resp.page.content: List<SurveyListItemDto>
                val elements = resp.page.content.map { it.address ?: "(주소 없음)" }

                allDataList.clear()
                allDataList.addAll(elements)

                // 기본 정렬: 최신순 (updatedAt 기준 소팅이 필요하면 서버에서 정렬해주는 게 정확)
                applySorting("최신순")
            } catch (e: Exception) {
                Toast.makeText(this@DataTransmissionActivity, "목록 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                // 실패 시 빈 화면 처리
                allDataList.clear()
                applySorting("최신순")
            }
        }
    }

    /** 정렬 적용 */
    private fun applySorting(sortType: String) {
        sortedDataList.clear()
        when (sortType) {
            "최신순" -> sortedDataList.addAll(allDataList.asReversed())
            "과거순" -> sortedDataList.addAll(allDataList)
        }
        updateUI()
    }

    /** UI 갱신 */
    private fun updateUI() {
        if (sortedDataList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
            adapter.notifyDataSetChanged()
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
        popupView.findViewById<MaterialButton>(R.id.btnSurveyScheduled)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java)); popup.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnResurveyTarget)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java)); popup.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnSurveyHistory)?.setOnClickListener {
            startActivity(Intent(this, TransmissionCompleteActivity::class.java)); popup.dismiss()
        }
        popupView.findViewById<MaterialButton>(R.id.btnNotTransmitted)?.setOnClickListener {
            // 현재 화면
            popup.dismiss()
        }
    }
}
