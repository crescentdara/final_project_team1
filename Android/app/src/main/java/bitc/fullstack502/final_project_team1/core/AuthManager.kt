package bitc.fullstack502.final_project_team1.core

import android.content.Context
import bitc.fullstack502.final_project_team1.network.dto.LoginResponse

object AuthManager {
    private const val PREF = "auth"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "userId"
    private const val KEY_USERNAME = "username"
    private const val KEY_NAME = "name"
    private const val KEY_ROLE = "role"
    private const val KEY_LOGIN_TIME = "login_time"
    private const val KEY_EMP_NO = "emp_no"   // ✅ 사번 키 추가

    fun save(context: Context, resp: LoginResponse) {
        val p = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
        p.putString(KEY_TOKEN, resp.token)
        p.putInt(KEY_USER_ID, resp.user?.id ?: -1)
        p.putString(KEY_USERNAME, resp.user?.username)
        p.putString(KEY_NAME, resp.name)
        p.putString(KEY_ROLE, resp.role)
        p.putString(KEY_EMP_NO, resp.user?.emp_no)   // ✅ 사번 저장
        p.putLong(KEY_LOGIN_TIME, System.currentTimeMillis())   // ✅ 로그인 시간 기록
        p.apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        val p = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val token = p.getString(KEY_TOKEN, null)
        val uid = p.getInt(KEY_USER_ID, -1)
        return !token.isNullOrEmpty() && uid > 0
    }

    fun isExpired(context: Context, maxAgeMillis: Long = 24L * 60 * 60 * 1000): Boolean {
        val p = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val loginTime = p.getLong(KEY_LOGIN_TIME, 0L)
        return loginTime <= 0 || (System.currentTimeMillis() - loginTime) > maxAgeMillis
    }

    // ── 접근자들 (UI/네트워킹에서 사용) ─────────────────────────
    fun userId(context: Context): Int =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt(KEY_USER_ID, -1)

    fun token(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_TOKEN, null)

    fun username(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_USERNAME, null)

    fun name(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_NAME, null)

    fun role(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_ROLE, null)

    fun empNo(context: Context): String =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_EMP_NO, "-") ?: "-"   // ✅ 사번 불러오기 (없으면 "-")
}