package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.adapters.UserAdapter
import com.aitechnologies.utripod.databinding.ActivityViewUsersBinding
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.ViewUsersProvider
import com.aitechnologies.utripod.ui.viewModels.ViewUsersViewModel
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import kotlinx.coroutines.ExperimentalCoroutinesApi

class ViewUsersActivity : AppCompatActivity() {
    private lateinit var binding:ActivityViewUsersBinding
    private var usernameList:ArrayList<String> = arrayListOf()
    private lateinit var viewModel: ViewUsersViewModel
    private lateinit var provider: ViewUsersProvider
    private val userAdapter by lazy { UserAdapter(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        provider = ViewUsersProvider(UserRepository())
        viewModel = ViewModelProvider(this,provider)[ViewUsersViewModel::class.java]


        usernameList = intent.getParcelableArrayListExtra<Parcelable>("usernameList") as ArrayList<String>

        viewModel.users.observe(this,{
            binding.progressCircular.visibility = GONE
            binding.rvUsers.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@ViewUsersActivity)
                adapter = userAdapter
            }
            userAdapter.setData(it)
        })


        viewModel.getUsers(usernameList)

        userAdapter.setOnUserClickListener @ExperimentalCoroutinesApi {
            if (it.username == getUsername()) {
                startActivity(Intent(this, MyProfileActivity::class.java))
            } else {
                startActivity(
                    Intent(this, OthersProfileActivity::class.java)
                        .putExtra("user", it)
                )
            }
        }

        binding.imgBack.setOnClickListener { onBackPressed() }

    }
}