package bitc.fullstack502.final_project_team1.network.dto

// ✅ 로그인 응답 데이터
data class LoginResponse(
    val success: Boolean,      // 로그인 성공 여부
    val message: String,       // 응답 메시지
    val token: String?,        // 성공시에만 값
    val name: String,          // 실명
    val role: String,          // 역할 (예: EDITOR)
    val user: UserInfo?        // 성공시에만 존재
)

// ✅ 사용자 정보
data class UserInfo(
    val id: Long,               // 서버: user_id
    val username: String,      // 사용자 계정 ID
    val name: String,          // 사용자 이름
    val role: String,          // 사용자 역할
    val emp_no: String?         // ✅ 추가: 사번 (없으면 null)
)