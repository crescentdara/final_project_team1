package bitc.fullstack502.final_project_team1.ui.message

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.MessageApiClient
import bitc.fullstack502.final_project_team1.network.dto.MessageDto
import bitc.fullstack502.final_project_team1.network.dto.MessageReadRequest
import bitc.fullstack502.final_project_team1.ui.BaseActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

/**
 * 메시지 보관함 화면
 * - 단체/개인 메시지 리스트 표시
 * - 메시지 클릭 시 상세 다이얼로그 + 읽음 처리
 */
class MessageInboxActivity : BaseActivity() {

    override fun bottomNavSelectedItemId(): Int = 0

    override fun onClickInbox() { }

    private lateinit var rvMessages: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: MessageListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_inbox)

        // 공통 헤더(메뉴/배지 로직 재사용)
        initHeader("알림")

        // 🔹 툴바의 네비게이션(뒤로가기 화살표) 제거
        findViewById<MaterialToolbar>(R.id.toolbar)?.apply {
            navigationIcon = null
            setNavigationOnClickListener(null)
        }

        // 🔹 하단 뒤로가기 FAB 클릭 시 finish()
        findViewById<FloatingActionButton>(R.id.fabBack)?.setOnClickListener {
            finish()
        }

        // RecyclerView 초기화
        rvMessages = findViewById(R.id.rvMessages)
        tvEmpty = findViewById(R.id.tvEmpty)

        adapter = MessageListAdapter { message ->
            // 메시지 클릭 -> 상세 다이얼로그 + 읽음 처리
            showMessageDetailDialog(message)
        }

        rvMessages.layoutManager = LinearLayoutManager(this)
        rvMessages.adapter = adapter

        // 메시지 로드
        loadMessages(tvEmpty, rvMessages, adapter)
    }

    /**
     * 메시지 리스트 로드
     */
    private fun loadMessages(tvEmpty: TextView, rv: RecyclerView, adapter: MessageListAdapter) {
        val userId = AuthManager.userId(this) ?: return

        lifecycleScope.launch {
            try {
                val response = MessageApiClient.service.getMessageList(userId)
                if (response.isSuccessful && response.body() != null) {
                    val messages = response.body()!!
                    if (messages.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        rvMessages.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        rvMessages.visibility = View.VISIBLE
                        adapter.submitList(messages)
                    }
                } else {
                    Toast.makeText(this@MessageInboxActivity, "메시지 로드 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MessageInboxActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 메시지 상세 다이얼로그 표시 + 읽음 처리
     */
    private fun showMessageDetailDialog(message: MessageDto) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(message.title)
            .setMessage(
                """
                발신: ${message.senderName}
                날짜: ${message.sentAt.substring(0, 16).replace("T", " ")}
                유형: ${if (message.isBroadcast) "단체" else "개인"}
                
                ${message.content}
                """.trimIndent()
            )
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()

        // 읽음 처리 (안읽은 메시지만)
        if (!message.readFlag) {
            markAsRead(message.messageId)
        }
    }

    /**
     * 메시지 읽음 처리
     */
    private fun markAsRead(messageId: Long) {
        val userId = AuthManager.userId(this) ?: return

        lifecycleScope.launch {
            try {
                val request = MessageReadRequest(messageId, userId)
                MessageApiClient.service.markAsRead(request)
                // 읽음 처리 후 리스트 갱신
                loadMessages(tvEmpty, rvMessages, adapter)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
