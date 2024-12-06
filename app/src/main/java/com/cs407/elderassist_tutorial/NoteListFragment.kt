package com.cs407.elderassist_tutorial

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cs407.elderassist_tutorial.data.NoteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import android.widget.Button
import com.cs407.elderassist_tutorial.utils.parseUserInfo

class NoteListFragment(
    private val injectedUserViewModel: UserViewModel? = null
) : Fragment() {

    private lateinit var greetingTextView: TextView

    private lateinit var userViewModel: UserViewModel

    private lateinit var noteDB: NoteDatabase

    private lateinit var userInfoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteDB = NoteDatabase.getDatabase(requireContext())
        userViewModel = if (injectedUserViewModel != null) {
            injectedUserViewModel
        } else {
            ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        }

        val userState = userViewModel.userState.value
        Log.d("NoteListFragment", "id: ${userState?.id}")
        Log.d("NoteListFragment", "userName: ${userState?.name}")
        Log.d("NoteListFragment", "passwd: ${userState?.passwd}")
        Log.d("NoteListFragment", "randomInfo: ${userState?.randomInfo}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_note_list, container, false)
        greetingTextView = view.findViewById(R.id.greetingTextView)
        userInfoTextView = view.findViewById(R.id.userInfoTextView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 绑定按钮
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        val deleteAccountButton = view.findViewById<Button>(R.id.deleteAccountButton)
        val editUserInfoButton = view.findViewById<Button>(R.id.editUserInfoButton) // 新增按钮

        // 设置按钮点击事件
        logoutButton.setOnClickListener {
            // Log Out 的逻辑
            userViewModel.setUser(UserState())
            findNavController().navigate(R.id.action_noteListFragment_to_loginFragment)
        }

        deleteAccountButton.setOnClickListener {
            // Delete Account 的逻辑
            deleteAccountAndLogout()
        }

        editUserInfoButton.setOnClickListener {
            findNavController().navigate(R.id.action_noteListFragment_to_editUserInfoFragment)
        }

        // 设置欢迎文本
        val userState = userViewModel.userState.value
        greetingTextView.text = getString(R.string.greeting_text, userState.name)

        // 显示用户信息
        val randomInfo = userState?.randomInfo
        if (!randomInfo.isNullOrEmpty()) {
            val userInfo = parseUserInfo(randomInfo) // 解析 JSON 信息
            userInfoTextView.text = """
            User Info:
            Username: ${userInfo.username}
            Age: ${userInfo.age}
            Location: ${userInfo.location}
            Preferences: ${userInfo.preferences.joinToString(", ")}
        """.trimIndent()
        } else {
            userInfoTextView.text = "No user info available."
        }
    }

    private fun deleteAccountAndLogout() {
        val userId = userViewModel.userState.value?.id ?: return
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val database = NoteDatabase.getDatabase(requireContext())
                    database.userDao().deleteUser(userId) // 删除用户

                    withContext(Dispatchers.Main) {
                        userViewModel.setUser(UserState()) // 清空用户状态
                        findNavController().navigate(R.id.action_noteListFragment_to_loginFragment)
                    }
                } catch (e: Exception) {
                    Log.e("NoteListFragment", "Error deleting user: ${e.message}")
                }
            }
        }
    }
}
