package bitc.fullstack502.final_project_team1.ui.transmission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.final_project_team1.R

/**
 * 📋 전송 완료 목록 어댑터
 * - 개선된 데이터 구조 지원
 * - 상태별 색상 표시
 * - 유지보수 용이한 코드 구조
 */
class CompletedListAdapter(
    private val items: List<TransmissionCompleteActivity.CompletedSurveyItem>,
    private val onItemClick: (TransmissionCompleteActivity.CompletedSurveyItem) -> Unit
) : RecyclerView.Adapter<CompletedListAdapter.ViewHolder>() {

    /**
     * 🏗️ ViewHolder - 각 아이템 뷰를 담당
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtAddress: TextView = itemView.findViewById(R.id.txtAddress)
        private val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        private val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)

        /**
         * 📄 데이터 바인딩
         */
        fun bind(item: TransmissionCompleteActivity.CompletedSurveyItem) {
            txtAddress.text = item.address
            txtDate.text = "전송일시: ${item.completedDate}"
            txtStatus.text = item.status
            
            // 상태별 색상 설정
            setStatusColor(item.status)
            
            // 클릭 이벤트
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(item)
                }
            }
        }

        /**
         * 🎨 상태별 색상 설정
         */
        private fun setStatusColor(status: String) {
            val context = itemView.context
            val colorRes = when (status) {
                "결재완료" -> android.R.color.holo_green_dark
                "처리중" -> android.R.color.holo_orange_dark
                "반려" -> android.R.color.holo_red_dark
                else -> android.R.color.darker_gray
            }
            
            txtStatus.setTextColor(context.getColor(colorRes))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_completed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}