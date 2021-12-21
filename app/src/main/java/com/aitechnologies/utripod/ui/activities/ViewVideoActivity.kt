package com.aitechnologies.utripod.ui.activities

import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.aitechnologies.utripod.databinding.ActivityViewVideoBinding
import com.aitechnologies.utripod.util.AppUtil.Companion.getLoadControl
import com.aitechnologies.utripod.util.UTripodApp.Companion.simpleCache
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource

class ViewVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewVideoBinding
    private lateinit var exoPlayer: ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setExoplayer()

    }

    private fun setExoplayer() {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
        DefaultDataSource.Factory(
            this,
            httpDataSourceFactory
        )
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        exoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .setLoadControl(getLoadControl())
            .build()

        exoPlayer.addMediaItem(MediaItem.fromUri(intent.getStringExtra("video").toString()))
        binding.playerView.player = exoPlayer
        exoPlayer.prepare()

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsLoadingChanged(isLoading: Boolean) {
                super.onIsLoadingChanged(isLoading)
                if (isLoading)
                    binding.progressCircular.visibility = VISIBLE
                else
                    binding.progressCircular.visibility = INVISIBLE
            }
        })

    }

    override fun onPause() {
        super.onPause()
        exoPlayer.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        exoPlayer.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

}