package bitc.fullstack502.final_project_team1.ui.transmission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.final_project_team1.R
import bitc.fullstack502.final_project_team1.network.dto.SurveyListItemDto

class NotTransmittedListAdapter(
    private val items: MutableList<SurveyListItemDto>,
    private val onItemClick: (SurveyListItemDto) -> Unit
) : RecyclerView.Adapter<NotTransmittedListAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvAddress = v.findViewById<TextView>(R.id.tvAddress)
        fun bind(item: SurveyListItemDto) {
            tvAddress.text = item.address ?: "(주소 없음)"
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_not_transmitted, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    fun submit(newItems: List<SurveyListItemDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
