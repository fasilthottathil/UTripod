package com.aitechnologies.utripod.uvis.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.adapters.PostCommentAdapter
import com.aitechnologies.utripod.databinding.ActivityPostCommentBinding
import com.aitechnologies.utripod.models.PostComment
import com.aitechnologies.utripod.repository.UvisRepository
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.aitechnologies.utripod.uvis.viewModels.UvisCommentViewModel
import com.aitechnologies.utripod.uvis.viewModels.UvisCommentViewModelProvider
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
class UvisCommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostCommentBinding
    private lateinit var uvisCommentViewModel: UvisCommentViewModel
    private val postCommentAdapter by lazy { PostCommentAdapter(this) }
    private var postComment = PostComment()
    private var postId = ""
    private var username = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uvisCommentViewModelProvider = UvisCommentViewModelProvider(UvisRepository(application))

        uvisCommentViewModel = ViewModelProvider(
            this,
            uvisCommentViewModelProvider
        )[UvisCommentViewModel::class.java]

        postId = intent.getStringExtra("id").toString()
        username = intent.getStringExtra("username").toString()

        setupRecyclerView()

        setupGiphy()

        setupEmoji()

        uvisCommentViewModel.getComments(postId)

        uvisCommentViewModel.comments.observe(this, { event ->
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

        uvisCommentViewModel.isDeleted.observe(this, @SuppressLint("NotifyDataSetChanged") {
            dismissProgress()
            if (it)
                shortToast("deleted")
            else {
                val intent = intent
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
            }
        })

        postCommentAdapter.setOnReplyClickListener {
            startActivity(
                Intent(this, UvisCommentReplyActivity::class.java)
                    .putExtra("id", it.id)
                    .putExtra("postId", it.postId)
                    .putExtra("isFirst", true)
            )
        }

        postCommentAdapter.setOnMoreClickListener { postComment, view, _ ->
            val popupMenu = PopupMenu(this, view)
            if (postComment.username == getUsername() || username == getUsername())
                popupMenu.inflate(R.menu.post_comment_my_menu)
            else
                popupMenu.inflate(R.menu.post_comment_others_menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.reply -> {
                        startActivity(
                            Intent(this, UvisCommentReplyActivity::class.java)
                                .putExtra("id", postComment.id)
                                .putExtra("postId", postId)
                                .putExtra("isFirst", true)
                        )
                    }
                    R.id.delete -> {
                        showProgress("Deleting", false)
                        uvisCommentViewModel.deleteComment(postComment.id, postId)
                    }
                }
                true
            }
            popupMenu.show()
        }

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

    private fun addComment(comment: String) {
        if (isConnected()) {
            postComment.timestamp = Timestamp.now()
            postComment.comment = comment
            postComment.id = System.currentTimeMillis().toString()
            postComment.username = getUsername()
            postComment.postId = postId

            binding.addComment.text.clear()

            uvisCommentViewModel.addComment(postComment)

        } else {
            shortToast("No connection")
        }

    }

    private fun hideLoading() {
        binding.progressCircular.visibility = INVISIBLE
    }

    private fun setupRecyclerView() {
        binding.rvComments.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@UvisCommentActivity)
            adapter = postCommentAdapter
        }
    }

    private fun showLoading() {
        binding.progressCircular.visibility = VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        AppUtil.releaseUtils()
        binding.rvComments.adapter = null
    }

}