package com.aitechnologies.utripod.util

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.os.Build
import androidx.core.app.NotificationCompat
import com.aitechnologies.utripod.util.Constants.CHANNEL_ID
import com.aitechnologies.utripod.util.Constants.CHANNEL_ID_OFFLINE
import com.aitechnologies.utripod.util.Constants.CHANNEL_NAME
import com.aitechnologies.utripod.util.Constants.CHANNEL_NAME_OFFLINE
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache

class UTripodApp : Application() {

    companion object {
        lateinit var simpleCache: SimpleCache
        const val exoPlayerCacheSize: Long = 90 * 1024 * 1024
        lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
        lateinit var exoDatabaseProvider: StandaloneDatabaseProvider
        lateinit var instance: UTripodApp

        @Synchronized
        fun getAppInstance(): UTripodApp {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        //exoplayer caching
        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize)
        exoDatabaseProvider = StandaloneDatabaseProvider(this)
        simpleCache = SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor, exoDatabaseProvider)

        //notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                IMPORTANCE_HIGH
            ).apply {
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

            }

            notificationManager.createNotificationChannel(notificationChannel)

            val offlineNotificationChannel = NotificationChannel(
                CHANNEL_ID_OFFLINE,
                CHANNEL_NAME_OFFLINE,
                IMPORTANCE_DEFAULT
            ).apply {
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

            }

            notificationManager.createNotificationChannel(offlineNotificationChannel)

        }


    }
}