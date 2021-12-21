package com.aitechnologies.utripod.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.adapters.ProfileTabAdapter
import com.aitechnologies.utripod.databinding.FragmentMyProfileBinding
import com.aitechnologies.utripod.models.SocialLinks
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.activities.*
import com.aitechnologies.utripod.ui.viewModels.MyProfileViewModel
import com.aitechnologies.utripod.ui.viewModels.MyProfileViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.logout
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MyProfileFragment : Fragment() {
    private var _binding: FragmentMyProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var myProfileViewModel: MyProfileViewModel
    private var users: Users = Users()
    private var socialLinks = SocialLinks()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMyProfileBinding.inflate(inflater, container, false)

        val userRepository = UserRepository()
        val myProfileViewModelProvider = MyProfileViewModelProvider(userRepository)

        myProfileViewModel = ViewModelProvider(
            this,
            myProfileViewModelProvider
        )[MyProfileViewModel::class.java]

        setupTab()

        myProfileViewModel.users.observe(viewLifecycleOwner, {
            hideLoading()
            if (it.isNotEmpty()) {
                users = it[0]
                binding.fullname.text = users.name + " (" + users.profession + ")"
                binding.username.text = users.username
                binding.followersnumber.text = users.followers.toString()
                binding.followingnumber.text = users.following.toString()
                binding.profession.text = users.bio
                binding.city.text = users.location
                if (!users.isVerified)
                    binding.verifed.visibility = INVISIBLE
                Glide.with(requireContext())
                    .load(users.profileUrl)
                    .error(R.drawable.ic_baseline_person_24)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.profile)
            }
        })

        myProfileViewModel.linkList.observe(viewLifecycleOwner, {
            if (it.isEmpty()) {
                binding.logocontainer.visibility = GONE
            } else {
                binding.logocontainer.visibility = VISIBLE
                socialLinks = it[0]
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

        myProfileViewModel.getUserProfile(requireContext().getUsername())
        myProfileViewModel.getSocialLinks(requireContext().getUsername())


        binding.menu.setOnClickListener {
            val view = inflate(requireContext(), R.layout.settings_bottom_sheet, null)
            val promote = view.findViewById<LinearLayoutCompat>(R.id.promote)
            val blocked = view.findViewById<LinearLayoutCompat>(R.id.blocked)
            val edit = view.findViewById<LinearLayoutCompat>(R.id.edit)
            val social = view.findViewById<LinearLayoutCompat>(R.id.social)
            val privacy = view.findViewById<LinearLayoutCompat>(R.id.privacy)
            val logout = view.findViewById<LinearLayoutCompat>(R.id.logout)

            val bottomSheetDialog =
                BottomSheetDialog(requireContext(), R.style.bottom_sheet_dialog_theme)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.setCanceledOnTouchOutside(true)
            bottomSheetDialog.show()

            edit.setOnClickListener {
                bottomSheetDialog.dismiss()
                startActivity(
                    Intent(requireContext(), SettingsActivity::class.java)
                        .putExtra("users", users)
                )
            }

            privacy.setOnClickListener {
                bottomSheetDialog.dismiss()
                startActivity(
                    Intent(requireContext(), PrivacyAndTermsActivity::class.java))
            }

            logout.setOnClickListener {
                bottomSheetDialog.dismissWithAnimation
                AlertDialog.Builder(requireContext()).apply {
                    setMessage("Do you want to logout?")
                    setNegativeButton("No") { d, _ -> d.cancel() }
                    setPositiveButton("Yes") { d, _ ->
                        d.cancel()
                        requireContext().logout()
                        FirebaseMessaging.getInstance()
                            .unsubscribeFromTopic(requireContext().getUsername())
                        startActivity(
                            Intent(requireContext(), SplashActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                    }
                }.create().show()
            }

            social.setOnClickListener {
                bottomSheetDialog.dismissWithAnimation
                startActivity(
                    Intent(requireContext(), SocialLinkActivity::class.java)
                        .putExtra("links", socialLinks)
                )
            }

            blocked.setOnClickListener {
                bottomSheetDialog.dismissWithAnimation
                startActivity(Intent(requireContext(), BlockedUsersActivity::class.java))
            }

            promote.setOnClickListener {
                startActivity(Intent(requireContext(), MyPromotionsActivity::class.java))
            }

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

        myProfileViewModel.usernameList.observe(viewLifecycleOwner,{
            dismissProgress()
            startActivity(Intent(requireContext(),ViewUsersActivity::class.java)
                .putExtra("usernameList",it))
        })

        binding.following.setOnClickListener {
            requireContext().showProgress("Loading...",false)
            myProfileViewModel.getFollowings(requireContext().getUsername())
        }

        binding.followers.setOnClickListener {
            requireContext().showProgress("Loading...",false)
            myProfileViewModel.getFollowers(requireContext().getUsername())
        }


        return binding.root
    }

    private fun viewLink(url: String) {

        startActivity(Intent().apply {
            action = Intent.ACTION_VIEW
            data = if (url.startsWith("http://",true) || url.startsWith("https://")){
                Uri.parse(url)
            }else{
                Uri.parse("https://$url")
            }
        })
    }

    @ExperimentalCoroutinesApi
    private fun setupTab() {
        binding.viewpager.apply {
            adapter = ProfileTabAdapter(
                requireContext().getUsername(),
                childFragmentManager,
                lifecycle
            )
            TabLayoutMediator(binding.tabLayout, this) { tab, position ->
                when (position) {
                    0 -> tab.text = "Posts"
                    1 -> tab.text = "Tags"
                    2-> tab.text = "Uvis"
                }
                binding.viewpager.currentItem = tab.position
            }.attach()
        }
    }

    private fun hideLoading() {
        binding.loadingLayout.visibility = GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewpager.adapter = null
        _binding = null
    }

}