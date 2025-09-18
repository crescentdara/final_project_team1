package bitc.fullstack502.final_project_team1.ui.survey

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.ui.surveyList.SurveyListActivity
import bitc.fullstack502.final_project_team1.ui.transmission.DataTransmissionActivity
import bitc.fullstack502.final_project_team1.ui.transmission.TransmissionCompleteActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class SurveyStatusActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var cardSurveyScheduled: MaterialCardView
    private lateinit var cardResurveyTarget: MaterialCardView
    private lateinit var cardTransmissionComplete: MaterialCardView
    private lateinit var cardNotTransmitted: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // ✅ 햄버거바 → 모달 카테고리 열기
        toolbar.setNavigationOnClickListener {
            showCategoryModal()
        }

        // ✅ 조사예정관리
        cardSurveyScheduled.setOnClickListener {
            Toast.makeText(this, "조사목록 관리 페이지 준비중", Toast.LENGTH_SHORT).show()
        }

        // ✅ 재조사 대상
        cardResurveyTarget.setOnClickListener {
            Toast.makeText(this, "재조사 대상 페이지 준비중", Toast.LENGTH_SHORT).show()
        }

        // ✅ 조사내역조회
        cardTransmissionComplete.setOnClickListener {
            startActivity(Intent(this, TransmissionCompleteActivity::class.java))
        }

        // ✅ 미전송
        cardNotTransmitted.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java))
        }
    }

    // ✅ 카테고리 모달
    private fun showCategoryModal() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.modal_category, null)
        dialog.setContentView(view)

        view.findViewById<MaterialButton>(R.id.btnSurveyScheduled)?.setOnClickListener {
            startActivity(Intent(this, SurveyStatusActivity::class.java))
            dialog.dismiss()
        }
        view.findViewById<MaterialButton>(R.id.btnResurveyTarget)?.setOnClickListener {
            startActivity(Intent(this, SurveyListActivity::class.java))
            dialog.dismiss()
        }
        view.findViewById<MaterialButton>(R.id.btnSurveyHistory)?.setOnClickListener {
            Toast.makeText(this, "조사결과 입력 페이지 준비중", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        view.findViewById<MaterialButton>(R.id.btnNotTransmitted)?.setOnClickListener {
            startActivity(Intent(this, DataTransmissionActivity::class.java))
            dialog.dismiss()
        }

        dialog.show()
    }

    // ✅ 홈버튼 처리 (← 버튼 눌렀을 때)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                showCategoryModal()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}