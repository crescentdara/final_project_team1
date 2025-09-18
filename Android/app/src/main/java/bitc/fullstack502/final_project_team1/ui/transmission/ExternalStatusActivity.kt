package bitc.fullstack502.final_project_team1.ui.transmission

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R
import java.util.*

class ExternalStatusActivity : AppCompatActivity() {

    private lateinit var editTextRemarks: EditText
    private val VOICE_REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_external_status)

        editTextRemarks = findViewById(R.id.editText_remarks)

        // 음성 입력 버튼
        val voiceButton = findViewById<Button>(R.id.button_voice_input)
        voiceButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "기타사항을 말해주세요")
            }
            startActivityForResult(intent, VOICE_REQUEST_CODE)
        }

        // 뒤로가기 버튼
        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { finish() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.firstOrNull()?.let {
                val currentText = editTextRemarks.text.toString()
                editTextRemarks.setText(if (currentText.isEmpty()) it else "$currentText\n$it")
            }
        }
    }
}
