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

    fun logout(context: Context) { // ✅ 편의 로그아웃
        clear(context)
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

    fun refreshLoginTime(context: Context) { // ✅ 세션 시간 갱신
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putLong(KEY_LOGIN_TIME, System.currentTimeMillis()).apply()
    }

    fun requireLoggedIn(context: Context) { // ✅ 보장용
        if (!isLoggedIn(context)) {
            throw IllegalStateException("로그인이 필요합니다.")
        }
    }

    // ── 접근자들 (UI/네트워킹에서 사용) ─────────────────────────
    fun userId(context: Context): Int =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt(KEY_USER_ID, -1)

    fun userIdOrThrow(context: Context): Int { // ✅ 없으면 예외
        val id = userId(context)
        if (id <= 0) throw IllegalStateException("유저 정보가 없습니다. 다시 로그인 해주세요.")
        return id
    }

    fun token(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_TOKEN, null)

    fun tokenOrThrow(context: Context): String { // ✅ 없으면 예외
        val t = token(context)
        if (t.isNullOrBlank()) throw IllegalStateException("토큰이 없습니다. 다시 로그인 해주세요.")
        return t
    }

    fun bearerOrThrow(context: Context): String { // ✅ Authorization 헤더용
        return "Bearer ${tokenOrThrow(context)}"
    }

    fun username(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_USERNAME, null)

    fun name(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_NAME, null)

    fun role(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_ROLE, null)

<<<<<<< HEAD
    fun empNo(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_EMP_NO, null) // ✅ 사번 불러오기
}
=======
    fun empNo(context: Context): String =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_EMP_NO, "-") ?: "-"   // ✅ 사번 불러오기 (없으면 "-")
}
>>>>>>> app/shs/DesignFeature
