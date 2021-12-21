package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.adapters.PostAdapter
import com.aitechnologies.utripod.databinding.ActivityPostSingleBinding
import com.aitechnologies.utripod.models.Posts
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.PostHashTagViewModel
import com.aitechnologies.utripod.ui.viewModels.PostHashTagViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showPostBottomSheetDialog
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi

class PostSingleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostSingleBinding
    private lateinit var postHashTagViewModel: PostHashTagViewModel
    private lateinit var postHashTagViewModelProvider: PostHashTagViewModelProvider
    private val postAdapter by lazy { PostAdapter(this) }
    private var posts: ArrayList<Posts> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostSingleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postHashTagViewModelProvider = PostHashTagViewModelProvider(
            PostRepository(application),
            UserRepository()
        )

        postHashTagViewModel = ViewModelProvider(
            this,
            postHashTagViewModelProvider
        )[PostHashTagViewModel::class.java]

        setupRecyclerView()

        postHashTagViewModel.posts.observe(this, {
            hideLoading()
            if (it.isNotEmpty()) {
                posts.addAll(it)
                postAdapter.setData(posts)
            } else shortToast("Post not found")
        })

        postHashTagViewModel.getPostById(intent.getStringExtra("id").toString())

        postHashTagViewModel.statusMessage.observe(this, {
            it.getContentIfNotHandled()?.let { loaded ->
                if (loaded)
                    hideLoading()
            }
        })

        postHashTagViewModel.sharePost.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                AppUtil.dismissProgress()
                if (it != "null")
                    startActivity(
                        Intent.createChooser(
                            Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, it)
                                type = "text/plain"
                            },
                            "Share to"
                        )
                    )
            }
        })

        postHashTagViewModel.isDeleted.observe(this, {
            AppUtil.dismissProgress()
            shortToast("Deleted")
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        })

        postHashTagViewModel.userProfile.observe(this, @ExperimentalCoroutinesApi {
            AppUtil.dismissProgress()
            if (it[0].username == getUsername()) {
                startActivity(Intent(this, MyProfileActivity::class.java))
            } else {
                startActivity(
                    Intent(this, OthersProfileActivity::class.java)
                        .putExtra("user", it[0])
                )
            }
        })

        postAdapter.onLikeClickListener {
            postHashTagViewModel.likePost(it)
        }


        postAdapter.onCommentClickListener @ExperimentalCoroutinesApi {
            startActivity(
                Intent(this, PostCommentActivity::class.java)
                    .putExtra("id", it.id)
                    .putExtra("username",it.username)
            )
        }

        postAdapter.onShareClickListener {
            showProgress("Loading", false)
            postHashTagViewModel.sharePost(it)
        }

        postAdapter.onMoreClickListener {
            if (it.username == getUsername()) {
                when (it.viewType) {
                    0 -> {
                        showPostBottomSheetDialog(0, it, false)
                    }
                    1 -> {
                        showPostBottomSheetDialog(0, it, false)
                    }
                    2 -> {
                        showPostBottomSheetDialog(0, it, false)
                    }
                    3 -> {
                        showPostBottomSheetDialog(1, it, false)
                    }
                    4 -> {
                        showPostBottomSheetDialog(1, it, false)
                    }
                    5 -> {
                        showPostBottomSheetDialog(1, it, false)
                    }
                }
            } else {
                showPostBottomSheetDialog(2, it)
            }
        }

        AppUtil.onClickBottomSheetItem { type, posts, isPromotion ->
            when (type) {
                0 -> postHashTagViewModel.sharePost(posts)
                1 -> {
                    postHashTagViewModel.reportPost(posts)
                    shortToast("reported")
                }
                2 -> {
                    showProgress("Deleting...", false)
                    if (isPromotion)
                        postHashTagViewModel.deletePromotion(posts)
                    else
                        postHashTagViewModel.deletePost(posts)
                }
                3 -> {
                    val bundle = Bundle().apply {
                        putParcelable("post", posts)
                        putInt("type", 0)
                    }
                    val intent = Intent(this, PromotePostActivity::class.java)
                    intent.putExtra("bundle", bundle)

                    startActivity(intent)
                }
                4 -> {
                    val bundle = Bundle().apply {
                        putParcelable("post", posts)
                    }
                    startActivity(
                        Intent(this, EditPostActivity::class.java)
                            .putExtra("bundle", bundle)
                    )
                }
            }
        }

        postAdapter.onImageClickListener {
            startActivity(
                Intent(
                    this,
                    ViewImageActivity::class.java
                ).putExtra("image", it.post)
            )
        }

        postAdapter.onUsernameClickListener { list, posts ->
            if (list.isEmpty()) {
                showProgress("Loading...", false)
                postHashTagViewModel.getUserProfile(posts.username.toString())
            } else {
                val arrayList: ArrayList<String> = arrayListOf()
                list.forEach {
                    arrayList.add(it)
                }
                startActivity(
                    Intent(this, TaggedUsersActivity::class.java)
                        .putExtra("list", arrayList)
                        .putExtra("bundle", Bundle().apply { putParcelable("post", posts) })
                )
            }
        }

        postAdapter.onVideoClickListener {
            startActivity(
                Intent(this, ViewVideoActivity::class.java)
                    .putExtra("video", it.post)
            )
        }

        binding.imgBack.setOnClickListener { onBackPressed() }

    }

    private fun hideLoading() {
        binding.progressCircular.visibility = View.INVISIBLE
    }

    private fun setupRecyclerView() {
        binding.rvPosts.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@PostSingleActivity)
            adapter = postAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        postAdapter.releaseExoplayer()
    }

}