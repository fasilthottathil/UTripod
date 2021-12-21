package com.aitechnologies.utripod.ui.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.aitechnologies.utripod.adapters.HashTagAdapter
import com.aitechnologies.utripod.adapters.PostAdapter
import com.aitechnologies.utripod.databinding.ActivityTrendingPostBinding
import com.aitechnologies.utripod.models.Posts
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.TrendingPostViewModel
import com.aitechnologies.utripod.ui.viewModels.TrendingPostViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showPostBottomSheetDialog
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi

class TrendingPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrendingPostBinding
    private val postAdapter by lazy { PostAdapter(this) }
    private lateinit var trendingPostViewModel: TrendingPostViewModel
    private lateinit var trendingPostViewModelProvider: TrendingPostViewModelProvider
    private var blockedUserList: ArrayList<String> = arrayListOf()
    private var posts: ArrayList<Posts> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrendingPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trendingPostViewModelProvider = TrendingPostViewModelProvider(
            PostRepository(application),
            UserRepository()
        )

        trendingPostViewModel = ViewModelProvider(
            this,
            trendingPostViewModelProvider
        )[TrendingPostViewModel::class.java]

        trendingPostViewModel.getTrendingHashtags()
        trendingPostViewModel.getBlockedUsers(getUsername())

        trendingPostViewModel.blockedUsers.observe(this, {
            blockedUserList.addAll(it)
            trendingPostViewModel.loadPosts(blockedUserList)
        })


        trendingPostViewModel.posts.observe(this, {
            if (it.isNotEmpty()) {
                it.forEach { post ->
                    var isContain = false
                    if (!blockedUserList.contains(post.username)) {
                        posts.forEach { p ->
                            if (p.id == post.id)
                                isContain = true
                        }
                    }
                    if (!isContain)
                        posts.add(post)
                }
                binding.rvPosts.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this@TrendingPostActivity)
                    adapter = postAdapter
                }
                postAdapter.setData(posts.sortedByDescending { post -> post.likes })
            }
        })


        trendingPostViewModel.hashtags.observe(this, {
            if (it.isNotEmpty()) {
                val hashTagAdapter = HashTagAdapter(it)
                binding.rvHashtags.apply {
                    setHasFixedSize(true)
                    layoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
                    adapter = hashTagAdapter
                }
                hashTagAdapter.setOnHashTagClickListener { hashtag ->
                    startActivity(
                        Intent(this, PostHashTagActivity::class.java)
                            .putExtra("hashtag", hashtag)
                    )
                }
            }
        })

        trendingPostViewModel.statusMessage.observe(this, {
            it.getContentIfNotHandled()?.let { loaded ->
                if (loaded)
                    hideLoading()
            }
        })

        trendingPostViewModel.sharePost.observe(this, { event ->
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

        trendingPostViewModel.isDeleted.observe(this, {
            AppUtil.dismissProgress()
            posts.remove(it.data)
            postAdapter.setData(posts)
        })

        trendingPostViewModel.userProfile.observe(this, @ExperimentalCoroutinesApi {
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
            trendingPostViewModel.likePost(it)
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
            trendingPostViewModel.sharePost(it)
        }

        postAdapter.onMoreClickListener {
            if (it.username == getUsername()) {
                when (it.viewType) {
                    0 -> {
                        showPostBottomSheetDialog(0, it)
                    }
                    1 -> {
                        showPostBottomSheetDialog(0, it)
                    }
                    2 -> {
                        showPostBottomSheetDialog(0, it)
                    }
                    3 -> {
                        showPostBottomSheetDialog(1, it)
                    }
                    4 -> {
                        showPostBottomSheetDialog(1, it)
                    }
                    5 -> {
                        showPostBottomSheetDialog(1, it)
                    }
                }
            } else {
                showPostBottomSheetDialog(2, it)
            }
        }

        AppUtil.onClickBottomSheetItem { type, selectedPost, isPromotion ->
            when (type) {
                0 -> trendingPostViewModel.sharePost(selectedPost)
                1 -> {
                    trendingPostViewModel.reportPost(selectedPost)
                    shortToast("reported")
                }
                2 -> {
                    showProgress("Deleting...", false)
                    if (isPromotion)
                        trendingPostViewModel.deletePromotion(selectedPost)
                    else
                        trendingPostViewModel.deletePost(selectedPost)
                }
                3 -> {
                    val bundle = Bundle().apply {
                        putParcelable("post", selectedPost)
                        putInt("type", 0)
                    }
                    val intent = Intent(this, PromotePostActivity::class.java)
                    intent.putExtra("bundle", bundle)

                    startActivity(intent)
                }
                4 -> {
                    val bundle = Bundle().apply {
                        putParcelable("post", selectedPost)
                    }
                    startActivity(
                        Intent(this, EditPostActivity::class.java)
                            .putExtra("bundle", bundle)
                    )
                }
                5 -> {
                    AlertDialog.Builder(this@TrendingPostActivity).apply {
                        setMessage("Do you want to block?")
                        setNegativeButton("No") { d, _ -> d.cancel() }
                        setPositiveButton("Yes") { d, _ ->
                            d.cancel()
                            trendingPostViewModel.blockUser(
                                getUsername(),
                                selectedPost.username!!
                            )
                            posts.remove(selectedPost)
                            postAdapter.setData(posts)
                        }
                    }.create().show()
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
                trendingPostViewModel.getUserProfile(posts.username.toString())
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
        binding.progressCircular.visibility = INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        postAdapter.releaseExoplayer()
        binding.rvHashtags.adapter = null
        AppUtil.releaseUtils()
    }

}