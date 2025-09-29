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
import bitc.fullstack502.final_project_team1.network.dto.ReturnTo
import bitc.fullstack502.final_project_team1.network.dto.SurveyListItemDto
import bitc.fullstack502.final_project_team1.ui.login.LoginActivity
import bitc.fullstack502.final_project_team1.ui.surveyList.BuildingInfoBottomSheet
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import bitc.fullstack502.final_project_team1.network.dto.EXTRA_RETURN_TO


class DataTransmissionActivity : AppCompatActivity() {

    // UI
    private lateinit var spinnerSort: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var adapter: NotTransmittedListAdapter
    private val allDataList    = mutableListOf<SurveyListItemDto>()
    private val sortedDataList = mutableListOf<SurveyListItemDto>()


    private val sortOptions = arrayOf("최신순", "오래된순")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_transmission)

        initViews()
        setupSort()
        setupRecyclerView()

        // ✅ 서버에서 임시저장(TEMP) 목록만 로드
        loadTempFromServer()
    }

    // ✅ 돌아올 때마다 항상 새로고침
    override fun onResume() {
        super.onResume()
        loadTempFromServer()
    }

    // ✅ CLEAR_TOP | SINGLE_TOP으로 재사용되어 포커스로 올라올 때도 새로고침
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        loadTempFromServer()
    }

    private fun initViews() {
        spinnerSort = findViewById(R.id.spinnerSort)
        recyclerView = findViewById(R.id.recyclerNotTransmittedList)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabBack)
            ?.setOnClickListener { onBackPressed() }
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
        // 어댑터는 클릭 콜백으로 SurveyListItemDto를 넘기도록
        adapter = NotTransmittedListAdapter(sortedDataList) { item ->
            // DTO 필드명이 프로젝트마다 다를 수 있어 안전하게 꺼냄
            val surveyId   = item.surveyId ?: return@NotTransmittedListAdapter
            val buildingId = item.buildingId ?: 0L
            val address    = item.address ?: ""

            BuildingInfoBottomSheet
                .newInstanceForTempDetail(
                    surveyId   = surveyId,
                    buildingId = buildingId,
                    address    = address
                ).apply {
                    arguments?.putString(EXTRA_RETURN_TO, ReturnTo.NOT_TRANSMITTED.name)
                }
                .show(supportFragmentManager, "tempDetail")
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
                    status = "TEMP",      // 임시저장만
                    page = 0,
                    size = 50
                )

                allDataList.clear()
                allDataList.addAll(resp.page.content)  // ← 주소 문자열로 바꾸지 말고 원본 DTO 그대로

                applySorting("최신순")
            } catch (e: Exception) {
                Toast.makeText(this@DataTransmissionActivity, "목록 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                allDataList.clear()
                applySorting("최신순")
            }
        }
    }

    private fun applySorting(sortType: String) {
        fun key(dt: String?) = dt ?: ""  // 필요 시 파싱 로직 넣어도 됨
        sortedDataList.clear()
        when (sortType) {
            "최신순" -> sortedDataList.addAll(allDataList.sortedByDescending { key(it.assignedAtIso) })
            "오래된순" -> sortedDataList.addAll(allDataList.sortedBy { key(it.assignedAtIso) })
            else     -> sortedDataList.addAll(allDataList)
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

}
