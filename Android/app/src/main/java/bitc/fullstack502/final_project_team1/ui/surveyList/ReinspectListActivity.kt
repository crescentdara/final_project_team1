// app/src/main/java/bitc/fullstack502/final_project_team1/ui/surveyList/ReinspectListActivity.kt
package bitc.fullstack502.final_project_team1.ui.surveyList

import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.ApiClient
import bitc.fullstack502.final_project_team1.network.dto.SurveyListItemDto
import kotlinx.coroutines.launch
import java.net.URLEncoder

class ReinspectListActivity : AppCompatActivity() {

    private lateinit var spinnerSort: Spinner
    private lateinit var listContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 공통 레이아웃 사용 (파일명이 다르면 여기만 바꿔주세요)
        setContentView(R.layout.activity_survey_list)

        spinnerSort = findViewById(R.id.spinnerSort)
        listContainer = findViewById(R.id.listContainer)

        // (선택) 상단 타이틀을 재조사로 바꾸고 싶다면
        // 레이아웃의 타이틀 TextView에 android:id="@+id/tvTitle"를 달고 아래 주석 해제
        // findViewById<TextView>(R.id.tvTitle)?.text = "재조사 대상"

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                loadAndRender()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        loadAndRender()
    }

    private fun loadAndRender() {
        lifecycleScope.launch {
            try {
                val auth = AuthManager.bearerOrThrow(this@ReinspectListActivity) // <-- 여기
                val res = ApiClient.service.getSurveysReJe(auth, page = 0, size = 200)

                var items = res.page.content
                when (spinnerSort.selectedItemPosition) {
                    1 -> items = items.sortedBy { it.address ?: "" }                // 주소순
                    2 -> items = items.sortedByDescending { it.updatedAtIso ?: "" } // 반려일자순
                }
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
            val v = inflater.inflate(R.layout.item_survey, listContainer, false)

            val tvAddress = v.findViewById<TextView>(R.id.tvAddress)
            val btnMap = v.findViewById<Button>(R.id.btnMap)
            val btnRoute = v.findViewById<Button>(R.id.btnRoute)

            tvAddress.text = item.address ?: (item.buildingName ?: "건물 #${item.buildingId}")

            // 지도보기
            btnMap.setOnClickListener {
                val q = URLEncoder.encode(item.address ?: item.buildingName ?: "", "UTF-8")
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$q")))
            }
            // 길찾기
            btnRoute.setOnClickListener {
                val q = URLEncoder.encode(item.address ?: item.buildingName ?: "", "UTF-8")
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$q")))
            }

            // 카드 클릭 → 건물정보 BottomSheet + 재조사 시작 버튼
            v.setOnClickListener {
                BuildingInfoBottomSheet.newInstanceForReinspect(
                    surveyId    = item.surveyId,
                    buildingId  = item.buildingId,
                    address     = item.address,
                    buildingName= item.buildingName,
                    rejectReason= item.rejectReason,
                    rejectedAt  = item.updatedAtIso
                ).show(supportFragmentManager, "building_info")
            }

            listContainer.addView(v)
        }
    }
}
