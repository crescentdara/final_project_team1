//package bitc.fullstack502.final_project_team1.ui.adapter
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import bitc.fullstack502.final_project_team1.R
//import bitc.fullstack502.final_project_team1.network.dto.AssignedBuilding
//import com.google.android.material.button.MaterialButton
//
///**
// * 조사 목록 RecyclerView Adapter
// * 서버에서 받은 AssignedBuilding 리스트를 item_survey.xml에 매핑
// */
//class SurveyAdapter(
//    private var surveyList: List<AssignedBuilding>
//) : RecyclerView.Adapter<SurveyAdapter.SurveyViewHolder>() {
//
//    // ✅ ViewHolder 정의
//    class SurveyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
//        val btnMap: MaterialButton = itemView.findViewById(R.id.btnMap)
//        val btnRoute: MaterialButton = itemView.findViewById(R.id.btnRoute)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_survey, parent, false)
//        return SurveyViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: SurveyViewHolder, position: Int) {
//        val survey = surveyList[position]
//
//        // ✅ 주소 출력
//        holder.tvAddress.text = survey.lotAddress ?: "주소 없음"
//
//        // ✅ 버튼 이벤트 (지도, 길찾기)
//        holder.btnMap.setOnClickListener {
//            // TODO: 지도 보기 기능 연결
//        }
//
//        holder.btnRoute.setOnClickListener {
//            // TODO: 길찾기 기능 연결
//        }
//    }
//
//    override fun getItemCount() = surveyList.size
//
//    // ✅ 리스트 갱신 메서드
//    fun updateData(newList: List<AssignedBuilding>) {
//        surveyList = newList
//        notifyDataSetChanged()
//    }
//}