package bitc.fullstack502.final_project_team1.ui.transmission

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.final_project_team1.R

/**
 * 📋 전송 완료 목록 어댑터
 * - TransmissionCompleteActivity 와 맞춤
 * - 상태별 색상 (결재완료: 파랑 / 결재대기: 초록)
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
            txtDate.text = "전송일: ${item.completedDate}"
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
            when (status) {
                "결재완료" -> txtStatus.setTextColor(Color.parseColor("#6898FF")) // 포인트 컬러
                "결재대기" -> txtStatus.setTextColor(context.getColor(android.R.color.holo_green_dark))
                else -> txtStatus.setTextColor(context.getColor(android.R.color.darker_gray))
            }
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
