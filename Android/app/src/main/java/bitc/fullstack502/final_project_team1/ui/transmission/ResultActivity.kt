package bitc.fullstack502.final_project_team1.ui.transmission

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import bitc.fullstack502.final_project_team1.ui.transmission.SurveyActivity
class ResultActivity : AppCompatActivity() {

    private lateinit var btnClose: ImageView
    private lateinit var btnEdit: MaterialButton
    private lateinit var btnSend: MaterialButton

    private lateinit var etManagementNumber: TextInputEditText
    private lateinit var etSurveyImpossible: TextInputEditText
    private lateinit var etAdministrativeUse: TextInputEditText
    private lateinit var etUtilizationType: TextInputEditText
    private lateinit var etIdleRatio: TextInputEditText
    private lateinit var etBuildingStatus: TextInputEditText
    private lateinit var etSafetyGrade: TextInputEditText
    private lateinit var etPhotoExistence: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_survey_result)

        // 뷰 초기화
        btnClose = findViewById(R.id.btnClose)
        btnEdit = findViewById(R.id.btnEdit)
        btnSend = findViewById(R.id.btnSend)

        etManagementNumber = findViewById(R.id.etManagementNumber)
        etSurveyImpossible = findViewById(R.id.etSurveyImpossible)
        etAdministrativeUse = findViewById(R.id.etAdministrativeUse)
        etIdleRatio = findViewById(R.id.etIdleRatio)
        etSafetyGrade = findViewById(R.id.etSafetyGrade)
        etPhotoExistence = findViewById(R.id.etPhotoExistence)

        // SurveyActivity에서 넘어온 데이터 받기
        val surveyImpossible = intent.getStringExtra("surveyImpossible") ?: ""
        val administrativeUse = intent.getStringExtra("administrativeUse") ?: ""
        val idleRatio = intent.getStringExtra("idleRatio") ?: ""
        val safetyGrade = intent.getStringExtra("safetyGrade") ?: ""
        val photoPath = intent.getStringExtra("photoPath") ?: ""

        // EditText에 값 설정
        etSurveyImpossible.setText(surveyImpossible)
        etAdministrativeUse.setText(administrativeUse)
        etIdleRatio.setText(idleRatio)
        etSafetyGrade.setText(safetyGrade)
        etPhotoExistence.setText(if (photoPath.isNotEmpty()) "있음" else "없음")

        // 닫기 버튼
        btnClose.setOnClickListener { finish() }

        // 수정 버튼
        btnEdit.setOnClickListener {
            Toast.makeText(this, "결과 수정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SurveyActivity::class.java)
            startActivity(intent)
        }

        // 전송 버튼
        btnSend.setOnClickListener {
            val managementNumber = etManagementNumber.text.toString()
            val surveyImpossibleVal = etSurveyImpossible.text.toString()
            val administrativeUseVal = etAdministrativeUse.text.toString()
            val utilizationType = etUtilizationType.text.toString()
            val idleRatioVal = etIdleRatio.text.toString()
            val buildingStatus = etBuildingStatus.text.toString()
            val safetyGradeVal = etSafetyGrade.text.toString()
            val photoExistenceVal = etPhotoExistence.text.toString()

            val resultMsg = """
                관리번호: $managementNumber
                조사불가: $surveyImpossibleVal
                행정목적: $administrativeUseVal
                활용유형: $utilizationType
                유휴비율: $idleRatioVal
                건물현황: $buildingStatus
                안전등급: $safetyGradeVal
                사진유무: $photoExistenceVal
            """.trimIndent()

            Toast.makeText(this, "전송됨:\n$resultMsg", Toast.LENGTH_LONG).show()
        }
    }
}
