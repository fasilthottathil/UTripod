package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.adapters.TagUserRoundAdapter
import com.aitechnologies.utripod.adapters.UserAdapter
import com.aitechnologies.utripod.databinding.ActivityTagUserBinding
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.TagUserViewModel
import com.aitechnologies.utripod.ui.viewModels.TagUserViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast

class TagUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTagUserBinding
    private lateinit var tagUserViewModel: TagUserViewModel
    private val tagUserAdapter by lazy { UserAdapter(this) }
    private val tagUserRoundAdapter by lazy { TagUserRoundAdapter(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userRepository = UserRepository()
        val tagUserViewModelProvider = TagUserViewModelProvider(userRepository)


        tagUserViewModel = ViewModelProvider(
            this,
            tagUserViewModelProvider
        )[TagUserViewModel::class.java]

        binding.edtSearch.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_SEARCH && binding.edtSearch.text.isNotBlank()) {
                showLoading()
                tagUserViewModel.searchUser(binding.edtSearch.text.toString())
            }
            true
        }

        tagUserViewModel.users.observe(this, {
            hideLoading()
            if (it.isNotEmpty()) {
                tagUserViewModel.userList.clear()
                tagUserViewModel.userList.addAll(it)
                binding.rvSearch.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this@TagUserActivity)
                    adapter = tagUserAdapter
                }
                tagUserAdapter.setData(it)
            } else {
                shortToast("No user found")
            }
        })

        tagUserViewModel.usersSelected.observe(this, {
            if (it.isNotEmpty()) {
                binding.rvUsers.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(
                        this@TagUserActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    adapter = tagUserRoundAdapter
                }
                tagUserRoundAdapter.setData(it)
                binding.imgDone.visibility = VISIBLE
            } else {
                tagUserRoundAdapter.setData(it)
                binding.imgDone.visibility = INVISIBLE
            }
        })

        tagUserAdapter.setOnUserClickListener {
            if (it.username == getUsername())
                shortToast("Can't tag yourself")
            else
                tagUserViewModel.addToSelected(it)
        }

        tagUserRoundAdapter.setOnDeleteListener {
            tagUserViewModel.removeFromSelected(it)
        }

        binding.imgDone.setOnClickListener {
            val list: ArrayList<String> = arrayListOf()
            tagUserViewModel.usersSelectedList.forEach {
                list.add(it.username)
            }
            val intent = Intent().apply {
                putStringArrayListExtra("tags", list)
            }
            setResult(RESULT_OK, intent)
            finish()
        }


    }


    private fun hideLoading() {
        binding.progressCircular.visibility = INVISIBLE
    }

    private fun showLoading() {
        binding.progressCircular.visibility = VISIBLE
    }

}