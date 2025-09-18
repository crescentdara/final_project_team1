package bitc.fullstack502.final_project_team1.ui.survey

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import bitc.fullstack502.final_project_team1.R
import com.google.android.material.button.MaterialButton

class SurveyResultDialog(
    private val context: Context,
    private val address: String,
    private val onSendComplete: (() -> Unit)? = null // ✅ 콜백 추가
) {
    private lateinit var dialog: AlertDialog

    fun show() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_survey_result, null)

        // 닫기 버튼
        view.findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }

        // 결과 수정
        view.findViewById<MaterialButton>(R.id.btnEdit).setOnClickListener {
            Toast.makeText(context, "결과 수정 기능 실행", Toast.LENGTH_SHORT).show()
            // TODO: 수정 입력 화면 열기
        }

        // 전송하기
        view.findViewById<MaterialButton>(R.id.btnSend).setOnClickListener {
            Toast.makeText(context, "조사결과 전송 완료", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            // ✅ 서버 전송 후 완료 페이지 이동
            onSendComplete?.invoke()
        }

        dialog = AlertDialog.Builder(context)
            .setView(view)
            .create()

        dialog.show()
    }
}