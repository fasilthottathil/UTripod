package com.aitechnologies.utripod.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.adapters.PostCommentAdapter
import com.aitechnologies.utripod.databinding.ActivityPostCommentReplyBinding
import com.aitechnologies.utripod.models.PostComment
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.PostReplyCommentViewModel
import com.aitechnologies.utripod.ui.viewModels.PostReplyCommentViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.google.firebase.Timestamp
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class PostCommentReplyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostCommentReplyBinding
    private val postCommentAdapter by lazy { PostCommentAdapter(this) }
    private var postComment = PostComment()
    private var id = ""
    private var postId = ""
    private var username = ""
    private var isFirst = false
    private lateinit var postReplyCommentViewModel: PostReplyCommentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostCommentReplyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getStringExtra("id").toString()
        postId = intent.getStringExtra("postId").toString()
        isFirst = intent.getBooleanExtra("isFirst", false)
        username = intent.getStringExtra("username").toString()

        val postReplyCommentViewModelProvider =
            PostReplyCommentViewModelProvider(PostRepository(application), UserRepository())

        postReplyCommentViewModel = ViewModelProvider(
            this,
            postReplyCommentViewModelProvider
        )[PostReplyCommentViewModel::class.java]

        setupRecyclerView()

        setupGiphy()

        setupEmoji()


        postReplyCommentViewModel.getReplyComments(id)

        postReplyCommentViewModel.comments.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.message) {
                    "loading" -> showLoading()
                    "success" -> {
                        hideLoading()
                        if (it.data!!.isNotEmpty()) {
                            postCommentAdapter.setData(it.data.sortedBy { list -> list.timestamp })
                            binding.rvComments.smoothScrollToPosition(it.data.size - 1)
                        }
                    }
                    "error" -> hideLoading()
                }
            }
        })

        postCommentAdapter.setOnReplyClickListener {
            startActivity(
                Intent(this, PostCommentReplyActivity::class.java)
                    .putExtra("id", it.id)
                    .putExtra("postId", it.postId)
                    .putExtra("isFirst", false)
                    .putExtra("username",username)
            )
        }

        postReplyCommentViewModel.isDeleted.observe(this, @SuppressLint("NotifyDataSetChanged") {
            dismissProgress()
            postCommentAdapter.notifyDataSetChanged()
            if (it)
                shortToast("deleted")
            else
                shortToast("An error occurred")
        })

        postCommentAdapter.setOnMoreClickListener { comment, view, position ->

            val popupMenu = PopupMenu(this, view)
            if (comment.username == getUsername() || username == getUsername())
                popupMenu.inflate(R.menu.post_comment_my_menu)
            else
                popupMenu.inflate(R.menu.post_comment_others_menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.reply -> {
                        startActivity(
                            Intent(this, PostCommentReplyActivity::class.java)
                                .putExtra("id", comment.id)
                                .putExtra("postId", comment.postId)
                                .putExtra("isFirst", false)
                        )
                    }
                    R.id.delete -> {
                        showProgress("Deleting", false)
                        postReplyCommentViewModel.deletePostReplyComment(
                            comment.id,
                            comment.postId,
                            postId,
                            isFirst
                        )
                        postCommentAdapter.notifyItemRemoved(position)
                    }
                }
                true
            }
            popupMenu.show()
        }

        postCommentAdapter.onUserClickListener {
            showProgress("Loading...",false)
            postReplyCommentViewModel.getUser(it)
        }

        postReplyCommentViewModel.user.observe(this,{
            dismissProgress()
            if (it[0].username == getUsername()) {
                startActivity(Intent(this, MyProfileActivity::class.java))
            } else {
                startActivity(
                    Intent(this, OthersProfileActivity::class.java)
                        .putExtra("user", it[0])
                )
            }
        })

    }

    private fun setupEmoji() {
        EmojiManager.install(GoogleEmojiProvider())
        val popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.c)).build(binding.addComment)
        binding.imgEmoji.setOnClickListener { popup.toggle() }
    }

    private fun setupGiphy() {
        Giphy.configure(this, "hRFim3mqzqDN5bGa2j2ZucI86U6zrz9z")

        binding.imgGiph.setOnClickListener {
            GiphyDialogFragment.newInstance()
                .apply {
                    gifSelectionListener = object : GiphyDialogFragment.GifSelectionListener {
                        override fun didSearchTerm(term: String) {

                        }

                        override fun onDismissed(selectedContentType: GPHContentType) {

                        }

                        override fun onGifSelected(
                            media: Media,
                            searchTerm: String?,
                            selectedContentType: GPHContentType
                        ) {
                            postComment.viewType = 1
                            addComment(media.id)
                        }
                    }
                }.show(supportFragmentManager, "giphy_dialog")
        }

        binding.post.setOnClickListener {
            val comment = binding.addComment.text.toString()
            if (comment.isNotBlank() && comment.isNotEmpty()) {
                postComment.viewType = 0
                addComment(comment)
            }
        }

    }

    private fun hideLoading() {
        binding.progressCircular.visibility = View.INVISIBLE
    }

    private fun setupRecyclerView() {
        binding.rvComments.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@PostCommentReplyActivity)
            adapter = postCommentAdapter
        }
    }

    private fun showLoading() {
        binding.progressCircular.visibility = View.VISIBLE
    }

    private fun addComment(comment: String) {
        if (isConnected()) {
            postComment.timestamp = Timestamp.now()
            postComment.comment = comment
            postComment.id = System.currentTimeMillis().toString()
            postComment.username = getUsername()
            postComment.postId = id

            binding.addComment.text.clear()

            postReplyCommentViewModel.addReplyComment(
                postComment,
                postId,
                isFirst
            )
        } else {
            shortToast("No connection")
        }

    }

}