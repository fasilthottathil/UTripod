package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.databinding.ActivitySocialLinkBinding
import com.aitechnologies.utripod.models.SocialLinks
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.SocialLinkProvider
import com.aitechnologies.utripod.ui.viewModels.SocialLinkViewModel
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress

class SocialLinkActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySocialLinkBinding
    private var socialLinks = SocialLinks()
    private lateinit var socialLinkViewModel: SocialLinkViewModel
    private lateinit var socialLinkProvider: SocialLinkProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        socialLinks = intent.getSerializableExtra("links") as SocialLinks

        socialLinkProvider = SocialLinkProvider(UserRepository())
        socialLinkViewModel = ViewModelProvider(
            this,
            socialLinkProvider
        )[SocialLinkViewModel::class.java]


        setUI()

        socialLinkViewModel.isAdded.observe(this, {
            dismissProgress()
            shortToast("Updated")
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        })

        binding.savechanges.setOnClickListener {
            if (!isConnected()) {
                shortToast("No connection")
                return@setOnClickListener
            }
            socialLinks.insta = binding.insta.text.toString()
            socialLinks.fb = binding.fb.text.toString()
            socialLinks.twitter = binding.twitter.text.toString()
            socialLinks.linkedin = binding.linkedin.text.toString()
            socialLinks.youtube = binding.youtube.text.toString()

            if (socialLinks.insta!!.isNotEmpty() && socialLinks.insta!!.isNotBlank()) {
                if (!Patterns.WEB_URL.matcher(socialLinks.insta!!).matches()) {
                    binding.insta.error = "Invalid url"
                    return@setOnClickListener
                }
            }
            if (socialLinks.fb!!.isNotEmpty() && socialLinks.fb!!.isNotBlank()) {
                if (!Patterns.WEB_URL.matcher(socialLinks.fb!!).matches()) {
                    binding.fb.error = "Invalid url"
                    return@setOnClickListener
                }
            }
            if (socialLinks.twitter!!.isNotEmpty() && socialLinks.twitter!!.isNotBlank()) {
                if (!Patterns.WEB_URL.matcher(socialLinks.twitter!!).matches()) {
                    binding.twitter.error = "Invalid url"
                    return@setOnClickListener
                }
            }
            if (socialLinks.linkedin!!.isNotEmpty() && socialLinks.linkedin!!.isNotBlank()) {
                if (!Patterns.WEB_URL.matcher(socialLinks.linkedin!!).matches()) {
                    binding.linkedin.error = "Invalid url"
                    return@setOnClickListener
                }
            }
            if (socialLinks.youtube!!.isNotEmpty() && socialLinks.youtube!!.isNotBlank()) {
                if (!Patterns.WEB_URL.matcher(socialLinks.youtube!!).matches()) {
                    binding.youtube.error = "Invalid url"
                    return@setOnClickListener
                }
            }

            showProgress("Updating...", false)

            socialLinkViewModel.addSocialLinks(getUsername(), socialLinks)
        }

    }

    private fun setUI() {
        binding.fb.setText(socialLinks.fb)
        binding.insta.setText(socialLinks.insta)
        binding.youtube.setText(socialLinks.youtube)
        binding.linkedin.setText(socialLinks.linkedin)
        binding.twitter.setText(socialLinks.twitter)
    }
}