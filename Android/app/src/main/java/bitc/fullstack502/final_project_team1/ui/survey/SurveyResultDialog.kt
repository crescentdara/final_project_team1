package bitc.fullstack502.final_project_team1.ui.survey

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import bitc.fullstack502.final_project_team1.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SurveyResultDialog(
    private val context: Context,
    private val address: String
) {
    
    private lateinit var dialog: AlertDialog
    private lateinit var etManagementNumber: TextInputEditText
    private lateinit var etSurveyImpossible: TextInputEditText
    private lateinit var etAdministrativeUse: TextInputEditText
    private lateinit var etUtilizationType: TextInputEditText
    private lateinit var etIdleRatio: TextInputEditText
    private lateinit var etBuildingStatus: TextInputEditText
    private lateinit var etSafetyGrade: TextInputEditText
    private lateinit var etPhotoExistence: TextInputEditText
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSave: MaterialButton

    fun show() {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_survey_result, null)
        
        initViews(view)
        setupClickListeners()
        
        dialog = AlertDialog.Builder(context)
            .setView(view)
            .create()
        
        dialog.show()
    }

    private fun initViews(view: android.view.View) {
        etManagementNumber = view.findViewById(R.id.etManagementNumber)
        etSurveyImpossible = view.findViewById(R.id.etSurveyImpossible)
        etAdministrativeUse = view.findViewById(R.id.etAdministrativeUse)
        etUtilizationType = view.findViewById(R.id.etUtilizationType)
        etIdleRatio = view.findViewById(R.id.etIdleRatio)
        etBuildingStatus = view.findViewById(R.id.etBuildingStatus)
        etSafetyGrade = view.findViewById(R.id.etSafetyGrade)
        etPhotoExistence = view.findViewById(R.id.etPhotoExistence)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnSave = view.findViewById(R.id.btnSave)
    }

    private fun setupClickListeners() {
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            saveSurveyResult()
        }
    }

    private fun saveSurveyResult() {
        val managementNumber = etManagementNumber.text?.toString()?.trim().orEmpty()
        val surveyImpossible = etSurveyImpossible.text?.toString()?.trim().orEmpty()
        val administrativeUse = etAdministrativeUse.text?.toString()?.trim().orEmpty()
        val utilizationType = etUtilizationType.text?.toString()?.trim().orEmpty()
        val idleRatio = etIdleRatio.text?.toString()?.trim().orEmpty()
        val buildingStatus = etBuildingStatus.text?.toString()?.trim().orEmpty()
        val safetyGrade = etSafetyGrade.text?.toString()?.trim().orEmpty()
        val photoExistence = etPhotoExistence.text?.toString()?.trim().orEmpty()

        if (managementNumber.isEmpty()) {
            Toast.makeText(context, "관리번호를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: 서버에 조사결과 저장 로직 구현
        Toast.makeText(context, "조사결과가 저장되었습니다", Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }
}
