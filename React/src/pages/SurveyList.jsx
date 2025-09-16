import { useEffect, useState } from "react";
import axios from "axios";

function SurveyList() {
  const [regions, setRegions] = useState([]);

  useEffect(() => {
    axios.get("/land-survey/regions")
        .then(res => {
              console.log(res.data);
              setRegions(res.data);
        }

            )
        .catch(err => console.error(err));
  }, []);

  return (
      <div className="container mt-4">
        <h2>미배정 조사목록</h2>

        {/* 그냥 네모 박스 */}
        <div className="border mb-4 p-3 bg-light">그냥 네모 박스</div>

        <div className="row">
          {/* 왼쪽 */}
          <div className="col-md-4 d-flex flex-column gap-3">
            {/* 지도 */}
            <div className="border" style={{ height: "200px", background: "#f5f5f5" }}>
              <p className="text-center pt-5">지도</p>
            </div>

            {/* 대상자 조회 */}
            <div className="p-3 border bg-light">
              <h5>대상자 조회</h5>
              <button className="btn btn-secondary w-100 mb-2">배정</button>
              <button className="btn btn-secondary w-100">배정</button>
            </div>
          </div>

          {/* 오른쪽 */}
          <div className="col-md-8">
            <div className="p-3 border bg-light">
              <h5>미배정 조사지 목록</h5>
              <p>총 {regions.length}건</p>
              <ul className="list-group" style={{ maxHeight: "400px", overflowY: "auto" }}>
                {/*{regions.map((region, index) => (*/}
                {/*    <li key={index} className="list-group-item">*/}
                {/*      <input type="checkbox" className="form-check-input me-2" />*/}
                {/*      {region}*/}
                {/*    </li>*/}
                {/*))}*/}
              </ul>
            </div>
          </div>
        </div>
      </div>
  );
}

export default SurveyList;
