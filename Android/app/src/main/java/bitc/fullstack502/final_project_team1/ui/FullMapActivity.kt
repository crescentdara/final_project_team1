package bitc.fullstack502.final_project_team1.ui

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.final_project_team1.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMapOptions

class FullMapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullmap)

        // 1) MapFragment 동적 추가 (수명주기 자동 관리 → 간편)
        val fm = supportFragmentManager
        var mapFragment = fm.findFragmentById(R.id.map_container) as MapFragment?
        if (mapFragment == null) {
            val opts = NaverMapOptions()
                .camera(CameraPosition(LatLng(37.5666102, 126.9783881), 14.0)) // 서울 시청 근처
            mapFragment = MapFragment.newInstance(opts)
            fm.beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit()
        }

        // 2) 버튼(아직은 동작만 찍어둠)
        val btn1: Button = findViewById(R.id.btnRadius1)
        val btn2: Button = findViewById(R.id.btnRadius2)
        val btn3: Button = findViewById(R.id.btnRadius3)

        btn1.setOnClickListener { /* TODO: 반경 1km 표시 */ }
        btn2.setOnClickListener { /* TODO: 반경 2km 표시 */ }
        btn3.setOnClickListener { /* TODO: 반경 3km 표시 */ }

        // 참고) 실제 NaverMap 객체가 필요하면:
        // mapFragment.getMapAsync { naverMap ->
        //     // naverMap.uiSettings.isLocationButtonEnabled = true  등 설정 가능
        // }
    }
}
