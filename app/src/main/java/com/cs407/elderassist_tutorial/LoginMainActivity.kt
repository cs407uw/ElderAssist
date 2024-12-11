package com.cs407.elderassist_tutorial
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.ViewModelProvider

class LoginMainActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginmain)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        // 从 SharedPreferences 获取登录状态
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)
        val userName = sharedPreferences.getString("NAME", null)
        val userId = sharedPreferences.getString("ID", null)?.toIntOrNull()
        val password = sharedPreferences.getString("PASSWORD", null)
        val randomInfo = sharedPreferences.getString("INFO", null)

        if (userName != null && userId != null) {
            userViewModel.setUser(UserState(userId, userName, password ?: "", randomInfo ?: ""))
        }
        Log.d("Login1",isLoggedIn.toString())
        if (isLoggedIn) {
            // 如果已登录，跳转到 NoteListFragment
            navigateToNoteListFragment()
        } else {
            // 如果未登录，跳转到 LoginFragment
            navigateToLoginFragment()
        }
    }

    private fun navigateToNoteListFragment() {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.findNavController()
            ?.navigate(R.id.noteListFragment)
    }

    private fun navigateToLoginFragment() {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.findNavController()
            ?.navigate(R.id.loginmain)
    }
}
