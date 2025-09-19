package bitc.fullstack502.final_project_team1.ui.transmission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.final_project_team1.R

class NotTransmittedListAdapter(
    private val items: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<NotTransmittedListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtAddress: TextView = itemView.findViewById(R.id.txtAddress)

        fun bind(addr: String) {
            txtAddress.text = addr

            // 🔹 아이템 전체 클릭 시 이벤트 전달
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(addr)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_not_transmitted, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}