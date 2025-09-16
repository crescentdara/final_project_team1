package bitc.fullstack502.final_project_team1.network.dto

data class LoginResponse(
    val success: Boolean,      // 로그인 성공 여부
    val message: String,       // 응답 메시지
    val token: String?,        // 성공시에만 값
    val name: String,          // 실명
    val role: String,          // 역할 (예: EDITOR)
    val user: UserInfo?        // 성공시에만 존재
)

data class UserInfo(
    val id: Int,               // 서버: Integer (user_id)
    val username: String,
    val name: String,
    val role: String
)
