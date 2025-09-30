package bitc.fullstack502.final_project_team1.ui.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.network.dto.MessageDto

/**
 * 메시지 리스트 어댑터
 * - 단체/개인 구분 표시
 * - 읽음/안읽음 상태 표시
 */
class MessageListAdapter(
    private val onItemClick: (MessageDto) -> Unit
) : ListAdapter<MessageDto, MessageListAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTypeBadge: TextView = itemView.findViewById(R.id.tvTypeBadge)
        private val tvReadBadge: TextView = itemView.findViewById(R.id.tvReadBadge)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvSender: TextView = itemView.findViewById(R.id.tvSender)

        fun bind(message: MessageDto, onItemClick: (MessageDto) -> Unit) {
            // 단체/개인 뱃지
            if (message.isBroadcast) {
                tvTypeBadge.text = "단체"
                tvTypeBadge.setBackgroundResource(R.drawable.badge_broadcast)
            } else {
                tvTypeBadge.text = "개인"
                tvTypeBadge.setBackgroundResource(R.drawable.badge_personal)
            }

            // 읽음 표시 (안읽음일 때만 표시)
            if (!message.readFlag) {
                tvReadBadge.visibility = View.VISIBLE
                tvReadBadge.text = "안읽음"
            } else {
                tvReadBadge.visibility = View.GONE
            }

            // 날짜 (ISO 문자열에서 날짜만 추출)
            tvDate.text = message.sentAt.substring(0, 10) // "2025-09-30"

            // 제목/내용
            tvTitle.text = message.title
            tvContent.text = message.content

            // 발신자
            tvSender.text = "발신: ${message.senderName}"

            // 클릭 이벤트
            itemView.setOnClickListener {
                onItemClick(message)
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<MessageDto>() {
        override fun areItemsTheSame(oldItem: MessageDto, newItem: MessageDto): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: MessageDto, newItem: MessageDto): Boolean {
            return oldItem == newItem
        }
    }
}
