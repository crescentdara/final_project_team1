package bitc.fullstack502.final_project_team1.network.dto

data class LoginResponse(
    val success: Boolean,      // 로그인 성공 여부
    val message: String,       // 응답 메시지
    val token: String?,        // JWT 토큰 (성공시에만)
    val user: UserInfo?        // 사용자 정보 (성공시에만)
)

data class UserInfo(
    val id: Long,              // 사용자 ID
    val username: String,      // 사용자명
    val name: String,          // 실명
    val role: String,          // 역할 (admin, user 등)
    val email: String?,        // 이메일
    val phone: String?         // 전화번호
)