package com.cs407.elderassist_tutorial.utils
import com.google.gson.Gson
import android.util.Log

data class UserInfo(
    val username: String="",
    val age: Int=0,
    val location: String="",
    val preferences: List<String> = emptyList()
)

/**
 * 生成用户信息字符串
 * 将用户输入的信息（用户名、年龄、位置、偏好）转换为 JSON 字符串存储
 * @param username 用户名
 * @param age 年龄
 * @param location 地址
 * @param preferences 偏好（以逗号分隔的字符串）
 * @return JSON 字符串形式的用户信息
 */
fun generateUserInfo(username: String, age: Int, location: String, preferences: String): String {
    // 将偏好字符串转为列表（用逗号分隔）
    val preferencesList = preferences.split(",").map { it.trim() }

    val userInfo = UserInfo(
        username = username,
        age = age,
        location = location,
        preferences = preferencesList
    )
    return Gson().toJson(userInfo) // 序列化为 JSON 字符串
}

/**
 * 解析用户信息
 * 将存储的 JSON 字符串解析为 UserInfo 对象
 * @param randomInfo JSON 字符串
 * @return UserInfo 数据类
 */
fun parseUserInfo(randomInfo: String): UserInfo {
    if (randomInfo.isNullOrEmpty()) {
        Log.w("UserInfoUtils", "randomInfo is empty or null.")
        return UserInfo() // 返回默认的空 UserInfo 对象
    }

    return try {
        Gson().fromJson(randomInfo, UserInfo::class.java)
    } catch (e: Exception) {
        Log.e("UserInfoUtils", "Failed to parse randomInfo: ${e.message}")
        UserInfo() // 返回默认的空 UserInfo 对象
    }
}

/**
 * 将 `UserInfo` 对象转换为字符串
 * 方便显示到用户界面
 * @param userInfo 用户信息对象
 * @return 格式化的字符串
 */
fun formatUserInfo(userInfo: UserInfo): String {
    val preferencesString = userInfo.preferences.joinToString(", ")
    return """
        Username: ${userInfo.username}
        Age: ${userInfo.age}
        Location: ${userInfo.location}
        Preferences: $preferencesString
    """.trimIndent()
}