package com.cs407.elderassist_tutorial

import android.content.Context
import android.content.Intent
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
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

class NoteListFragment(
    private val injectedUserViewModel: UserViewModel? = null
) : Fragment() {

    private lateinit var greetingTextView: TextView
    private lateinit var ageTextView: TextView
    private lateinit var locTextView: TextView
    private lateinit var userViewModel: UserViewModel
    private lateinit var backToHomeButton: ImageView
    private lateinit var noteDB: NoteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteDB = NoteDatabase.getDatabase(requireContext())
        userViewModel = if (injectedUserViewModel != null) {
            injectedUserViewModel
        } else {
            ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        }

        val userState = userViewModel.userState.value
        Log.d("homeFragment", "id: ${userState}")
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
        ageTextView = view.findViewById(R.id.ageTextView)
        locTextView = view.findViewById(R.id.locTextView)
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
            val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("IS_LOGGED_IN", false) // 更新为未登录状态
            editor.apply()
            userViewModel.setUser(UserState())
            findNavController().navigate(R.id.action_noteListFragment_to_loginFragment)
        }

        // 初始化按钮
        backToHomeButton = view.findViewById<ImageView>(R.id.backToHomeImage)

        // 设置点击事件
        backToHomeButton.setOnClickListener {
            val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val userState = userViewModel.userState.value
            editor.putString("NAME", userState.name)
            editor.putString("PASSWORD", userState.passwd)
            editor.putString("ID", userState.id.toString())
            editor.putString("INFO", userState.randomInfo)
            editor.putBoolean("IS_LOGGED_IN", true) // 更新为登录状态
            editor.apply()
            val intent = Intent(requireContext(), HomeActivity::class.java)
            startActivity(intent)

            // 如果希望关闭当前 LoginActivity 页面，防止返回堆栈中残留：
            requireActivity().finish()
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

        // 显示用户信息
        val randomInfo = userState?.randomInfo
        if (!randomInfo.isNullOrEmpty()) {
            val userInfo = parseUserInfo(randomInfo) // 解析 JSON 信息
            val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("NAME", userState.name)
            editor.putString("PASSWORD", userState.passwd)
            editor.putString("ID", userState.id.toString())
            editor.putString("INFO", userState.randomInfo)
            editor.putBoolean("IS_LOGGED_IN", true) // 更新为未登录状态
            editor.apply()
            Log.d("NoteList", "id: ${userState?.id}")
            Log.d("NoteList", "userName: ${userState?.name}")
            Log.d("NoteList", "passwd: ${userState?.passwd}")
            Log.d("NoteList", "randomInfo: ${userState?.randomInfo}")
            greetingTextView.text = getString(R.string.greeting_text, userInfo.username)
            ageTextView.text = getString(R.string.age_text, userInfo.age)
            locTextView.text = getString(R.string.loc_text, userInfo.location)
            lifecycleScope.launch {
                val userState = userViewModel.userState.value
                val preferences = userState?.randomInfo?.let { parseUserInfo(it).preferences } ?: emptyList()

                // 根据用户 preferences 筛选分类数据
                val (medicines, pharmacies, others) = withContext(Dispatchers.IO) {
                    filterPreferences(preferences)
                }

                // 显示分类数据
                displayItems(medicines, R.id.med, "medicine")
                displayItems(pharmacies, R.id.ph, "pharmacy")
                displayItems(others, R.id.oth, "other")
            }
        }


    }

    private fun deleteAccountAndLogout() {
        val userId = userViewModel.userState.value?.id ?: return
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // 更新为未登录状态
        editor.apply()
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

    // 数据筛选逻辑
    private suspend fun filterPreferences(preferences: List<String>): Triple<List<String>, List<String>, List<String>> {
        val medicationDao = noteDB.medicationDao()
        val pharmacyDao = noteDB.pharmacyDao()

        val medicines = mutableListOf<String>()
        val pharmacies = mutableListOf<String>()
        val others = mutableListOf<String>()

        for (preference in preferences) {
            val isMedicine = medicationDao.getMedicationByName(preference) != null
            val isPharmacy = pharmacyDao.getPharmacyByName(preference) != null

            when {
                isMedicine -> medicines.add(preference)
                isPharmacy -> pharmacies.add(preference)
                else -> others.add(preference)
            }
        }

        return Triple(medicines, pharmacies, others)
    }

    // 分类数据显示
    private fun displayItems(items: List<String>, containerId: Int, type: String) {
        val container = view?.findViewById<ViewGroup>(containerId)
        container?.removeAllViews()

        items.chunked(3).forEach { rowItems ->
            val row = LinearLayout(requireContext())
            row.orientation = LinearLayout.HORIZONTAL

            rowItems.forEach { item ->
                val textView = TextView(requireContext()).apply {
                    text = item
                    setPadding(16, 16, 16, 16)
                    // 设置字体颜色
                    setTextColor(
                        when (type) {
                            "medicine", "pharmacy" -> ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
                            else -> ContextCompat.getColor(requireContext(), android.R.color.black)
                        }
                    )

                    setOnClickListener {
                        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("IS_LOGGED_IN", true) // 更新为未登录状态
                        editor.apply()
                        navigateToMap(item.trim(), type.trim())
                        Log.d("Preference",item.trim())
                        Log.d("Preference",type.trim())
                    }
                }
                row.addView(textView)
            }

            container?.addView(row)
        }
    }

    // 跳转到 MapActivity
    private fun navigateToMap(name: String, type: String) {
        val intent = Intent(requireContext(), MapActivity::class.java)
        intent.putExtra("SEARCH_TYPE", type)
        intent.putExtra("SEARCH_NAME", name)
        startActivity(intent)
    }
}
