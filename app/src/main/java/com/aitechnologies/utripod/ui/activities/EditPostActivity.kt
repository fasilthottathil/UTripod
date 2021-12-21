package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.databinding.ActivityEditPostBinding
import com.aitechnologies.utripod.models.Posts
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.ui.viewModels.EditPostViewModel
import com.aitechnologies.utripod.ui.viewModels.EditPostViewModelProvider
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.bumptech.glide.Glide

class EditPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditPostBinding
    private var posts = Posts()
    private lateinit var editPostViewModel: EditPostViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val postRepository = PostRepository(application)

        val editPostViewModelProvider = EditPostViewModelProvider(postRepository, application)

        editPostViewModel = ViewModelProvider(
            this,
            editPostViewModelProvider
        )[EditPostViewModel::class.java]

        posts = intent.getBundleExtra("bundle")!!.getParcelable<Posts>("post") as Posts

        setUI()


        editPostViewModel.validate.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.data!!.response) {
                    0 -> binding.edtPost.error = it.message
                    1 -> shortToast(it.message.toString())
                    2 -> {
                        showLoading()
                        editPostViewModel.updatePost()
                    }
                }
            }
        })

        editPostViewModel.isUpdated.observe(this, {
            hideLoading()
            shortToast("Updated")
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            finish()
        })

        binding.update.setOnClickListener {
            if (posts.type == 0)
                posts.post = binding.edtPost.text.toString()
            posts.hashTags = binding.edtHashTags.text.toString()
            editPostViewModel.posts = posts
            editPostViewModel.validate()
        }

    }

    private fun hideLoading() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.progressCircular.visibility = INVISIBLE
    }

    private fun showLoading() {
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.progressCircular.visibility = VISIBLE
    }

    private fun setUI() {
        var hashTags = ""
        posts.hashTags!!.replace("[", "")
            .replace("]", "")
            .split(",")
            .forEach {
                if (it.isNotEmpty() && it.isNotBlank())
                    hashTags += "#$it"
            }
        binding.edtHashTags.setText(hashTags)
        if (posts.type == 0) {
            binding.edtPost.setText(posts.post)
        } else {
            Glide.with(applicationContext)
                .load(posts.post)
                .placeholder(R.drawable.image_place_holder)
                .into(binding.imgBack)

        }

        editPostViewModel.posts = posts

    }
}