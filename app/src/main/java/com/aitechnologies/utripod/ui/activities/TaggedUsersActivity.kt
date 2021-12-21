package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.adapters.TaggedUserAdapter
import com.aitechnologies.utripod.databinding.ActivityTaggedUsersBinding
import com.aitechnologies.utripod.models.Posts
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.TaggedUsersViewModel
import com.aitechnologies.utripod.ui.viewModels.TaggedUsersViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi

class TaggedUsersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaggedUsersBinding
    private lateinit var taggedUsersViewModel: TaggedUsersViewModel
    private lateinit var taggedUsersViewModelProvider: TaggedUsersViewModelProvider
    private var username = ""
    private val taggedUserAdapter by lazy { TaggedUserAdapter(this, username) }
    private var deleteList: ArrayList<String> = arrayListOf()
    private var posts = Posts()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaggedUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val list = intent.getStringArrayListExtra("list")!!
        posts = intent.getBundleExtra("bundle")!!.getParcelable<Posts>("post") as Posts
        username = posts.username.toString()

        taggedUsersViewModelProvider = TaggedUsersViewModelProvider(
            UserRepository(),
            PostRepository(application)
        )
        taggedUsersViewModel = ViewModelProvider(
            this,
            taggedUsersViewModelProvider
        )[TaggedUsersViewModel::class.java]

        taggedUsersViewModel.users.observe(this, {
            binding.progressCircular.visibility = INVISIBLE
            binding.rvUsers.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@TaggedUsersActivity)
                adapter = taggedUserAdapter
            }
            taggedUserAdapter.setData(it)
        })

        taggedUsersViewModel.getUsersByUsernameList(list)

        taggedUserAdapter.setOnUserClickListener @ExperimentalCoroutinesApi {
            if (it.username == getUsername()) {
                startActivity(Intent(this, MyProfileActivity::class.java))
            } else {
                startActivity(
                    Intent(this, OthersProfileActivity::class.java)
                        .putExtra("user", it)
                )
            }
        }

        taggedUserAdapter.setOnUserLongClickListener {
            if (deleteList.contains(it.username))
                deleteList.remove(it.username)
            else
                deleteList.add(it.username)
            if (deleteList.isEmpty())
                binding.imgDelete.visibility = INVISIBLE
            else
                binding.imgDelete.visibility = VISIBLE
        }

        binding.imgDelete.setOnClickListener {
            val tagList = arrayListOf<String>()
            posts.tags.toString().replace("[", "")
                .replace("]", "")
                .split(",")
                .forEach { str ->
                    deleteList.forEach {
                        if (it != str.trim())
                            tagList.add(it)
                    }
                }
            posts.tags = tagList.toString()

            showProgress("Updating...", false)

            taggedUsersViewModel.updateTaggedPost(posts, deleteList)

        }

        taggedUsersViewModel.updated.observe(this, {
            dismissProgress()
            shortToast("Updated")
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        })

        binding.imgBack.setOnClickListener { onBackPressed() }

    }
}