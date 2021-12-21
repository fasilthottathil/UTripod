package com.aitechnologies.utripod.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.aitechnologies.utripod.adapters.UvisAdapter
import com.aitechnologies.utripod.databinding.FragmentMyUvisPromotionsBinding
import com.aitechnologies.utripod.models.Uvis
import com.aitechnologies.utripod.models.UvisModel
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.repository.UvisRepository
import com.aitechnologies.utripod.ui.activities.MainActivity
import com.aitechnologies.utripod.ui.activities.MyProfileActivity
import com.aitechnologies.utripod.ui.activities.OthersProfileActivity
import com.aitechnologies.utripod.ui.activities.PromotePostActivity
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.showUvisBottomSheetDialog
import com.aitechnologies.utripod.util.UTripodApp
import com.aitechnologies.utripod.uvis.activities.AudioActivity
import com.aitechnologies.utripod.uvis.activities.EditUvisActivity
import com.aitechnologies.utripod.uvis.activities.UvisCommentActivity
import com.aitechnologies.utripod.uvis.viewModels.UvisViewModel
import com.aitechnologies.utripod.uvis.viewModels.UvisViewModelProvider
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MyUvisPromotions : Fragment() {
    private var _binding: FragmentMyUvisPromotionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var typeUvis: UvisAdapter.TypeUvis
    private lateinit var typeUvisPromotion: UvisAdapter.TypeUvisPromotion
    private var followingList: ArrayList<String> = arrayListOf()
    private val uvisAdapter by lazy { UvisAdapter(requireContext(), followingList) }
    private var uvisModel: ArrayList<UvisModel> = arrayListOf()
    private lateinit var uvisViewModel: UvisViewModel
    private lateinit var uvisViewModelProvider: UvisViewModelProvider
    private lateinit var exoPlayer: ExoPlayer
    private var canPlay = true
    private val simpleCache = UTripodApp.simpleCache
    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory
    private lateinit var permissionResultLauncher: ActivityResultLauncher<Array<String>>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMyUvisPromotionsBinding.inflate(inflater, container, false)

        uvisViewModelProvider = UvisViewModelProvider(
            UvisRepository(requireActivity().application),
            UserRepository()
        )


        uvisViewModel = ViewModelProvider(
            this,
            uvisViewModelProvider
        )[UvisViewModel::class.java]

        permissionResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            var isGranted = false
            it.forEach { (_, granted) ->
                if (!granted)
                    isGranted = false
            }

            if (isGranted) {
                requireContext().shortToast("Permission granted")
            } else {
                requireContext().shortToast("Permission denied")
            }
        }

        setupViewPager()

        setExoplayer()

        uvisAdapter.setOnUvisAttachedToWindowListener {
            typeUvis = it
        }

        uvisAdapter.setOnUvisPromotionAttachedToWindowListener {
            typeUvisPromotion = it
        }

        uvisAdapter.setOnFollowClickListener {
            uvisViewModel.followOrUnfollow(it, requireActivity().application)
        }

        uvisViewModel.getFollowings(requireContext().getUsername())

        uvisViewModel.followings.observe(viewLifecycleOwner, {
            followingList.addAll(it)
            uvisViewModel.getMyPromotions()
        })

        uvisViewModel.uvis.observe(viewLifecycleOwner, {
            hideLoading()
            if (it.isNotEmpty()) {
                it.forEach { uvis ->
                    uvisModel.add(
                        UvisModel(
                            uvis.id,
                            uvis.username,
                            uvis.profileUrl,
                            uvis.url,
                            uvis.likes,
                            uvis.comments,
                            uvis.shares,
                            uvis.description,
                            uvis.hashTags,
                            uvis.tags,
                            uvis.likesList,
                            uvis.isPublic,
                            uvis.timestamp,
                            uvis.profession.toString(),
                            uvis.viewType
                        )
                    )
                    val mediaItem = MediaItem.fromUri(uvis.url.toString())
                    exoPlayer.addMediaItem(mediaItem)
                    exoPlayer.prepare()
                }
                uvisAdapter.setData(uvisModel)
            } else {
                binding.txtNo.visibility = VISIBLE
            }


        })

        uvisViewModel.statusMessage.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { loaded ->
                if (loaded)
                    hideLoading()
            }
        })


        uvisViewModel.sharePost.observe(viewLifecycleOwner, { event ->
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


        uvisViewModel.isDeleted.observe(viewLifecycleOwner, {
            AppUtil.dismissProgress()
            requireContext().shortToast("Deleted")
            startActivity(
                Intent(requireContext(), MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
//            for (i in 0 until uvisModel.size) {
//                if (uvisModel[i].id == it.data!!.id) {
//                    uvisModel.removeAt(i)
//                    exoPlayer.removeMediaItem(i)
//                    break
//                }
//            }
//            uvisAdapter.setData(uvisModel)
        })

        uvisViewModel.userProfile.observe(viewLifecycleOwner, @ExperimentalCoroutinesApi {
            AppUtil.dismissProgress()
            if (it[0].username == requireContext().getUsername()) {
                startActivity(Intent(requireContext(), MyProfileActivity::class.java))
            } else {
                startActivity(
                    Intent(requireContext(), OthersProfileActivity::class.java)
                        .putExtra("user", it[0])
                )
            }
        })

        uvisAdapter.setOnLikeClickListener {
            uvisViewModel.likeUvis(getUvisFromModel(it))
        }


        uvisAdapter.setOnCommentClickListener {
            startActivity(
                Intent(requireContext(), UvisCommentActivity::class.java)
                    .putExtra("id", it.id)
            )
        }


        uvisAdapter.setOnMoreClickListener {
            if (it.username == requireContext().getUsername()) {
                when (it.viewType) {
                    0 -> {
                        requireContext().showUvisBottomSheetDialog(0, it)
                    }
                    1 -> {
                        requireContext().showUvisBottomSheetDialog(1, it)
                    }
                }
            } else {
                requireContext().showUvisBottomSheetDialog(2, it)
            }
        }

        AppUtil.onClickBottomSheetItemUvis { type, uvisModel, isPromotion ->
            when (type) {
                0 -> {
                    requireContext().showProgress("Loading...", false)
                    uvisViewModel.shareUvis(getUvisFromModel(uvisModel))
                }
                1 -> {
                    uvisViewModel.reportUvis(getUvisFromModel(uvisModel))
                    requireContext().shortToast("reported")
                }
                2 -> {
                    requireContext().showProgress("Deleting...", false)
                    if (isPromotion)
                        uvisViewModel.deletePromotion(getUvisFromModel(uvisModel))
                    else
                        uvisViewModel.deletePost(getUvisFromModel(uvisModel))
                }
                3 -> {
                    startActivity(
                        Intent(requireContext(), PromotePostActivity::class.java)
                            .putExtra("bundle", Bundle().apply {
                                putParcelable("uvis", getUvisFromModel(uvisModel))
                                putBoolean("isFromHome", true)
                                putInt("type", 1)
                            })
                    )
                }
                4 -> {
                    val bundle = Bundle().apply {
                        putParcelable("uvis", getUvisFromModel(uvisModel))
                    }
                    startActivity(
                        Intent(requireContext(), EditUvisActivity::class.java)
                            .putExtra("bundle", bundle)
                    )
                }
                5 -> {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        AppUtil.downloadFile(requireContext(), uvisModel.url!!)
                    } else {
                        permissionResultLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        )
                    }
                }
            }
        }

        uvisAdapter.setOnUsernameClickListener {
            requireContext().showProgress("Loading", false)
            uvisViewModel.getUserProfile(it.username.toString())
        }

        uvisAdapter.setOnProfileImageClickListener {
            requireContext().showProgress("Loading", false)
            uvisViewModel.getUserProfile(it.username.toString())
        }

        uvisAdapter.setOnMusicClickListener {
            startActivity(
                Intent(requireContext(), AudioActivity::class.java)
                    .putExtra("postUrl", it)
            )
        }

        return binding.root
    }

    private fun getUvisFromModel(uvisModel: UvisModel): Uvis {
        val uvis = Uvis()
        uvis.id = uvisModel.id
        uvis.viewType = uvisModel.viewType
        uvis.description = uvisModel.description
        uvis.hashTags = uvisModel.hashTags
        uvis.profileUrl = uvisModel.profileUrl
        uvis.username = uvisModel.username
        uvis.url = uvisModel.url
        uvis.likes = uvisModel.likes
        uvis.shares = uvisModel.shares
        uvis.tags = uvisModel.tags
        uvis.likesList = uvisModel.likesList
        uvis.timestamp = uvisModel.timestamp
        uvis.comments = uvisModel.comments
        return uvis
    }

    private fun setExoplayer() {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
        DefaultDataSource.Factory(
            requireContext(),
            httpDataSourceFactory
        )
        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        exoPlayer = ExoPlayer.Builder(requireContext())
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .setLoadControl(AppUtil.getLoadControl())
            .build()
        exoPlayer.playWhenReady = false
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
    }

    private fun hideLoading() {
        binding.progressCircular.visibility = View.INVISIBLE
    }

    private fun setupViewPager() {
        binding.viewpager.apply {
            orientation = ViewPager2.ORIENTATION_VERTICAL
            adapter = uvisAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (uvisModel.isNotEmpty()) {
                        exoPlayer.pause()
                        exoPlayer.seekTo(position, 0)
                        if (uvisModel[position].viewType == 0) {
                            typeUvis.binding.playerView.useController = false
                            typeUvis.binding.playerView.player = null
                            typeUvis.binding.playerView.player = exoPlayer
                        }
                        if (uvisModel[position].viewType == 1) {
                            typeUvisPromotion.binding.playerView.useController = false
                            typeUvisPromotion.binding.playerView.player = null
                            typeUvisPromotion.binding.playerView.player = exoPlayer
                        }
                        exoPlayer.play()
                        exoPlayer.addListener(object : Player.Listener {
                            override fun onPlayerStateChanged(
                                playWhenReady: Boolean,
                                playbackState: Int
                            ) {
                                if (playbackState == Player.STATE_READY) {
                                    if (canPlay)
                                        exoPlayer.play()
                                }
                            }

                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                super.onIsPlayingChanged(isPlaying)
                                if (isPlaying) {
                                    if (uvisModel[position].viewType == 0)
                                        typeUvis.binding.progressBar.visibility = View.INVISIBLE
                                    else
                                        typeUvisPromotion.binding.progressBar.visibility =
                                            View.INVISIBLE
                                } else {
                                    if (uvisModel[position].viewType == 0)
                                        typeUvis.binding.progressBar.visibility = VISIBLE
                                    else
                                        typeUvisPromotion.binding.progressBar.visibility =
                                            VISIBLE
                                }
                            }
                        })
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewpager.adapter = null
        _binding = null
        exoPlayer.release()
    }

    override fun onPause() {
        super.onPause()
        canPlay = false
        exoPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        canPlay = true
        exoPlayer.playWhenReady = canPlay
    }

}