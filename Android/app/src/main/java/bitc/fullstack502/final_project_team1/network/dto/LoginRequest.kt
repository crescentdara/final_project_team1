package bitc.fullstack502.final_project_team1.network.dto

data class LoginRequest(
    val id: String,  // 서버 record LoginRequest(id, pw)
    val pw: String
)
