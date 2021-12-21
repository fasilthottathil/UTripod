package com.aitechnologies.utripod.uvis.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.aitechnologies.utripod.adapters.UvisAdapter
import com.aitechnologies.utripod.databinding.ActivityUvisSingleBinding
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
import com.aitechnologies.utripod.util.AppUtil.Companion.downloadFile
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.showUvisBottomSheetDialog
import com.aitechnologies.utripod.util.UTripodApp
import com.aitechnologies.utripod.uvis.viewModels.UvisHashTagViewModel
import com.aitechnologies.utripod.uvis.viewModels.UvisHashTagViewModelProvider
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi

class UvisSingleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUvisSingleBinding
    private lateinit var uvisHashTagViewModel: UvisHashTagViewModel
    private lateinit var uvisHashTagViewModelProvider: UvisHashTagViewModelProvider
    private lateinit var exoPlayer: ExoPlayer
    private var canPlay = true
    private val simpleCache = UTripodApp.simpleCache
    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory
    private lateinit var typeUvis: UvisAdapter.TypeUvis
    private lateinit var typeUvisPromotion: UvisAdapter.TypeUvisPromotion
    private var followingList:ArrayList<String> = arrayListOf()
    private val uvisAdapter by lazy { UvisAdapter(this,followingList) }
    private var uvisModel: ArrayList<UvisModel> = arrayListOf()
    private lateinit var permissionResultLauncher: ActivityResultLauncher<Array<String>>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUvisSingleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uvisHashTagViewModelProvider = UvisHashTagViewModelProvider(
            UvisRepository(application),
            UserRepository()
        )

        uvisHashTagViewModel = ViewModelProvider(
            this,
            uvisHashTagViewModelProvider
        )[UvisHashTagViewModel::class.java]


        permissionResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            var isGranted = false
            it.forEach { (_, granted) ->
                if (!granted)
                    isGranted = false
            }

            if (isGranted) {
                shortToast("Permission granted")
            } else {
                shortToast("Permission denied")
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
            uvisHashTagViewModel.followOrUnfollow(it,application)
        }

        uvisHashTagViewModel.getFollowings(getUsername())

        uvisHashTagViewModel.followings.observe(this,{
            followingList.addAll(it)
            uvisHashTagViewModel.getPostById(intent.getStringExtra("id").toString())
        })

        uvisHashTagViewModel.uvis.observe(this, {
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
                }
                exoPlayer.prepare()
                uvisAdapter.setData(uvisModel)
            }
        })

        uvisHashTagViewModel.statusMessage.observe(this, {
            it.getContentIfNotHandled()?.let { loaded ->
                if (loaded)
                    hideLoading()
            }
        })



        uvisHashTagViewModel.sharePost.observe(this, { event ->
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


        uvisHashTagViewModel.isDeleted.observe(this, @ExperimentalCoroutinesApi {
            AppUtil.dismissProgress()
            shortToast("Deleted")
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )

        })

        uvisHashTagViewModel.userProfile.observe(this, @ExperimentalCoroutinesApi {
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

        uvisAdapter.setOnLikeClickListener {
            uvisHashTagViewModel.likeUvis(getUvisFromModel(it))
        }


        uvisAdapter.setOnCommentClickListener @ExperimentalCoroutinesApi {
            startActivity(
                Intent(this, UvisCommentActivity::class.java)
                    .putExtra("id", it.id)
                    .putExtra("username",it.username)
            )
        }


        uvisAdapter.setOnMoreClickListener {
            if (it.username == getUsername()) {
                when (it.viewType) {
                    0 -> {
                        showUvisBottomSheetDialog(0, it)
                    }
                    1 -> {
                        showUvisBottomSheetDialog(1, it)
                    }
                }
            } else {
                showUvisBottomSheetDialog(2, it)
            }
        }

        AppUtil.onClickBottomSheetItemUvis { type, uvisModel, isPromotion ->
            when (type) {
                0 -> {
                    showProgress("Loading...", false)
                    uvisHashTagViewModel.shareUvis(getUvisFromModel(uvisModel))
                }
                1 -> {
                    uvisHashTagViewModel.reportUvis(getUvisFromModel(uvisModel))
                    shortToast("reported")
                }
                2 -> {
                    showProgress("Deleting...", false)
                    if (isPromotion)
                        uvisHashTagViewModel.deletePromotion(getUvisFromModel(uvisModel))
                    else
                        uvisHashTagViewModel.deletePost(getUvisFromModel(uvisModel))
                }
                3 -> {
                    startActivity(
                        Intent(this, PromotePostActivity::class.java)
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
                        Intent(this, EditUvisActivity::class.java)
                            .putExtra("bundle", bundle)
                    )
                }
                5 -> {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        downloadFile(this, uvisModel.url!!)
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
            showProgress("Loading", false)
            uvisHashTagViewModel.getUserProfile(it.username.toString())
        }

        uvisAdapter.setOnProfileImageClickListener {
            showProgress("Loading", false)
            uvisHashTagViewModel.getUserProfile(it.username.toString())
        }

        uvisAdapter.setOnMusicClickListener {
            startActivity(
                Intent(this, AudioActivity::class.java)
                    .putExtra("postUrl", it)
            )
        }


        binding.imgBack.setOnClickListener { onBackPressed() }
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
            this,
            httpDataSourceFactory
        )
        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        exoPlayer = ExoPlayer.Builder(this)
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
                                        typeUvis.binding.progressBar.visibility = View.VISIBLE
                                    else
                                        typeUvisPromotion.binding.progressBar.visibility =
                                            View.VISIBLE
                                }
                            }
                        })
                    }
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        canPlay = false
        exoPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        canPlay = true
        exoPlayer.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        binding.viewpager.adapter = null
        AppUtil.releaseUtils()
    }

}