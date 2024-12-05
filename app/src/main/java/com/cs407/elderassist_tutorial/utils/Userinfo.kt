package com.cs407.elderassist_tutorial.utils
import com.google.gson.Gson
data class UserInfo(
    val age: Int,
    val location: String,
    val preferences: List<String>
)
/**
 * 动态生成用户的个性化信息
 * @param userName 用户名（如果需要和用户相关信息，可以作为参数）
 * @return JSON 字符串形式的用户信息
 */
fun generateUserInfo(userName: String): String {
    val preferences = listOf("BoomBoom Nasal Stick", "Dulcolax", "AZO",
                            "Voltaren", "Mucinex DM", "DayQuil/NyQuil",
                            "Isopropyl Alcohol", "MiraLAX", "Tylenol",
                            "NasalFresh", "Neosporin", "Iberogast",
                            "Zyrtec", "Advil", "XYZAL",
                            "Biofreeze", "Genexa", "Ricola",
                            "Imodium", "Abreva", "Halls",
                            "Vicks", "Prevacid", "Wonderbelly").shuffled().take(2)
    val location = listOf("Madison, WI", "New York, NY", "Chicago, IL").random()
    val age = (60..100).random()

    val userInfo = UserInfo(age = age, location = location, preferences = preferences)
    return Gson().toJson(userInfo) // 将对象序列化为 JSON
}

/**
 * 解析用户的 randomInfo 信息
 * @param randomInfo JSON 字符串
 * @return UserInfo 数据类
 */
fun parseUserInfo(randomInfo: String): UserInfo {
    return Gson().fromJson(randomInfo, UserInfo::class.java)
}
