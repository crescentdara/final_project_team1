package bitc.fullstack502.final_project_team1.core

import android.content.Context
import android.content.SharedPreferences
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

    private fun readLongCompat(ctx: Context, key: String, def: Long = -1L): Long {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return try {
            p.getLong(key, def)
        } catch (e: ClassCastException) {
            val any = p.all[key]
            val v = when (any) {
                is Long -> any
                is Int -> any.toLong()
                is String -> any.toLongOrNull() ?: def
                else -> def
            }
            if (v != def) p.edit().remove(key).putLong(key, v).apply() // 🔄 마이그레이션
            v
        }
    }



    fun save(context: Context, resp: LoginResponse) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_TOKEN, resp.token)
            putLong(KEY_USER_ID, resp.user?.id ?: -1L)
            putString(KEY_USERNAME, resp.user?.username)
            putString(KEY_NAME, resp.name)
            putString(KEY_ROLE, resp.role)
            putString(KEY_EMP_NO, resp.user?.emp_no)
            putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
        }.apply()
    }


    fun clear(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun logout(context: Context) { // ✅ 편의 로그아웃
        clear(context)
    }

    fun isLoggedIn(context: Context): Boolean {
        val token = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_TOKEN, null)
        val uid = readLongCompat(context, KEY_USER_ID, -1L)
        return !token.isNullOrEmpty() && uid > 0L
    }

    fun isExpired(context: Context, maxAgeMillis: Long = 24L * 60 * 60 * 1000) : Boolean {
        val loginTime = readLongCompat(context, KEY_LOGIN_TIME, 0L)
        return loginTime <= 0L || (System.currentTimeMillis() - loginTime) > maxAgeMillis
    }

    fun refreshLoginTime(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putLong(KEY_LOGIN_TIME, System.currentTimeMillis()).apply()
    }

    fun requireLoggedIn(context: Context) { // ✅ 보장용
        if (!isLoggedIn(context)) {
            throw IllegalStateException("로그인이 필요합니다.")
        }
    }

    // ── 접근자들 (UI/네트워킹에서 사용) ─────────────────────────
    fun userId(context: Context): Long =
        readLongCompat(context, KEY_USER_ID, -1L)

    fun userIdOrThrow(context: Context): Long {
        val id = userId(context)
        if (id <= 0L) throw IllegalStateException("유저 정보가 없습니다. 다시 로그인 해주세요.")
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

    fun empNo(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_EMP_NO, null) // ✅ 사번 불러오기
}

