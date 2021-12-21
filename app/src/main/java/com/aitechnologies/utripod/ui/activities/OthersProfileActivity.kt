package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.adapters.ProfileTabAdapter
import com.aitechnologies.utripod.databinding.ActivityOthersProfileBinding
import com.aitechnologies.utripod.models.SocialLinks
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.OthersProfileViewModel
import com.aitechnologies.utripod.ui.viewModels.OthersProfileViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class OthersProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOthersProfileBinding
    private var users = Users()
    private lateinit var othersProfileViewModel: OthersProfileViewModel
    private var socialLinks = SocialLinks()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOthersProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        users = intent.getSerializableExtra("user") as Users

        val othersProfileViewModelProvider = OthersProfileViewModelProvider(
            UserRepository(),
            ChatsRepository(),
            application
        )

        othersProfileViewModel = ViewModelProvider(
            this,
            othersProfileViewModelProvider
        )[OthersProfileViewModel::class.java]

        setUI()

        setupTab()

        othersProfileViewModel.isFollowing(users.username)
        othersProfileViewModel.getSocialLinks(users.username)

        othersProfileViewModel.isFollowing.observe(this, { event ->
            hideLoading()
            event.getContentIfNotHandled()?.let {
                if (it.message == "success") {
                    if (it.data!!) {
                        binding.txtFollow.setBackgroundResource(R.drawable.unfollow_button_profile)
                        "Unfollow".also { text -> binding.txtFollow.text = text }
                    } else {
                        binding.txtFollow.setBackgroundResource(R.drawable.follow_button_profile)
                        "Follow".also { text -> binding.txtFollow.text = text }
                    }
                }
            }
        })

        othersProfileViewModel.roomId.observe(this, {
            hideLoading()
            startActivity(
                Intent(this, PrivateChatActivity::class.java)
                    .putExtra("bundle", Bundle().apply { putParcelable("chats", it) })
            )
        })

        othersProfileViewModel.linkList.observe(this, {
            if (it.isEmpty()) {
                binding.logocontainer.visibility = View.GONE
            } else {
                socialLinks = it[0]
                binding.logocontainer.visibility = VISIBLE
                it.forEach { links ->
                    if (links.fb.toString() != "null" && links.fb.toString().isNotEmpty()) {
                        binding.fb.visibility = VISIBLE
                    }
                    if (links.twitter.toString() != "null" && links.twitter.toString()
                            .isNotEmpty()
                    ) {
                        binding.twitter.visibility = VISIBLE
                    }
                    if (links.youtube.toString() != "null" && links.youtube.toString()
                            .isNotEmpty()
                    ) {
                        binding.youtube.visibility = VISIBLE
                    }
                    if (links.linkedin.toString() != "null" && links.linkedin.toString()
                            .isNotEmpty()
                    ) {
                        binding.linkedin.visibility = VISIBLE
                    }
                    if (links.insta.toString() != "null" && links.insta.toString().isNotEmpty()) {
                        binding.insta.visibility = VISIBLE
                    }
                }
            }
        })

        othersProfileViewModel.blockOrUnblock.observe(this,{
            shortToast(it)
            dismissProgress()
        })

        binding.txtFollow.setOnClickListener {
            showLoading()
            othersProfileViewModel.followOrUnfollow(users.username)
        }

        binding.txtChat.setOnClickListener {
            showLoading()
            othersProfileViewModel.startChat(users)
        }

        binding.twitter.setOnClickListener {
            viewLink(socialLinks.twitter.toString())
        }
        binding.fb.setOnClickListener {
            viewLink(socialLinks.fb.toString())
        }
        binding.insta.setOnClickListener {
            viewLink(socialLinks.insta.toString())
        }
        binding.linkedin.setOnClickListener {
            viewLink(socialLinks.linkedin.toString())
        }
        binding.youtube.setOnClickListener {
            viewLink(socialLinks.youtube.toString())
        }

        binding.menu.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this,R.style.bottom_sheet_dialog_theme)
            val view = inflate(this,R.layout.others_settings_bottom_sheet,null)
            val share = view.findViewById<LinearLayoutCompat>(R.id.share)
            val block = view.findViewById<LinearLayoutCompat>(R.id.block)

            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()

            share.setOnClickListener {
                bottomSheetDialog.dismiss()
                startActivity(
                    Intent.createChooser(
                        Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "https://utripod.page.link/user/${users.username}")
                            type = "text/plain"
                        },
                        "Share to"
                    )
                )
            }

            block.setOnClickListener {
                bottomSheetDialog.dismiss()
                showProgress("Loading...",false)
                othersProfileViewModel.blockOrUnblock(users.username,getUsername())
            }

        }

        othersProfileViewModel.usernameList.observe(this,{
            dismissProgress()
            startActivity(Intent(this,ViewUsersActivity::class.java)
                .putExtra("usernameList",it))
        })

        binding.following.setOnClickListener {
            showProgress("Loading...",false)
            othersProfileViewModel.getFollowings(users.username)
        }

        binding.followers.setOnClickListener {
            showProgress("Loading...",false)
            othersProfileViewModel.getFollowers(users.username)
        }

    }

    private fun viewLink(url: String) {
        startActivity(Intent().apply {
            action = Intent.ACTION_VIEW
            data =  if (url.startsWith("http://",true) || url.startsWith("https://")){
                Uri.parse(url)
            }else{
                Uri.parse("https://$url")
            }
        })
    }

    private fun showLoading() {
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.progressCircular.visibility = VISIBLE
    }

    private fun hideLoading() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.progressCircular.visibility = INVISIBLE
        binding.loadingLayout.visibility = INVISIBLE
    }

    private fun setUI() {
        binding.fullname.text = users.name
        binding.username.text = users.username
        binding.followersnumber.text = users.followers.toString()
        binding.followingnumber.text = users.following.toString()
        binding.profession.text = users.profession
        binding.city.text = users.location
        if (!users.isVerified)
            binding.verifed.visibility = INVISIBLE
        Glide.with(applicationContext)
            .load(users.profileUrl)
            .error(R.drawable.ic_baseline_person_24)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.profile)
    }

    @ExperimentalCoroutinesApi
    private fun setupTab() {
        binding.viewpager.apply {
            adapter = ProfileTabAdapter(
                users.username,
                supportFragmentManager,
                lifecycle
            )
            TabLayoutMediator(binding.tabLayout, this) { tab, position ->
                when (position) {
                    0 -> tab.text = "Posts"
                    1 -> tab.text = "Tags"
                    2 -> tab.text = "Uvis"
                }
                binding.viewpager.currentItem = tab.position
            }.attach()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppUtil.releaseUtils()
        binding.viewpager.adapter = null
    }

}