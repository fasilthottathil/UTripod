package com.aitechnologies.utripod.uvis.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.databinding.ActivityVideoEditorBinding
import com.aitechnologies.utripod.util.AppUtil.Companion.utripodFile
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit


class VideoEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoEditorBinding
    private var videoPath = ""
    private var startTime = ""
    private var musicPath = ""
    private var duration = ""
    var boolean = false
    private var uploadFile: File? = null
    private lateinit var simpleExoPlayer: ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        simpleExoPlayer = ExoPlayer.Builder(this)
            .setLoadControl(DefaultLoadControl())
            .build()

        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        binding.player.player = simpleExoPlayer


        videoPath = intent.getStringExtra("video_path").toString()
        startTime =
            TimeUnit.MILLISECONDS.toSeconds(intent.getIntExtra("start_time", 0).toLong()).toString()
        musicPath = intent.getStringExtra("music_uri").toString()

        MediaPlayer.create(this, Uri.fromFile(File(videoPath))).also {
            duration = TimeUnit.MILLISECONDS.toSeconds(it.duration.toLong()).toString()
            it.reset()
            it.release()
        }



        if (intent.getStringExtra("music_uri").toString().isNotEmpty()) {
            //removing audio from video
            val location = utripodFile("Utripod_removed_audio${System.currentTimeMillis()}.mp4")
            val cmd: Array<String> = arrayOf(
                "-i",
                videoPath,
                "-vcodec",
                "copy",
                "-an",
                location.absolutePath
            )
            FFmpeg.executeAsync(
                cmd
            ) { _, _ ->
                addMusic(location.absolutePath)
            }
        } else {
            addWaterMark(videoPath)
        }

        binding.imgDone.setOnClickListener {
            startActivity(
                Intent(this, AddUvisActivity::class.java)
                    .putExtra("video_path", videoPath)
            )
            finish()
        }

    }

    private fun addWaterMark(finalVideo: String) {
        val bm =
            BitmapFactory.decodeResource(resources, R.drawable.logo)
        val extStorageDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString()
        val file = File(extStorageDirectory, "logo.png")
        if (!file.exists()) {
            try {
                val outStream = FileOutputStream(file)
                bm.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                outStream.flush()
                outStream.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        val location = utripodFile("Utripod_Final${System.currentTimeMillis()}.mp4")
        val cmd = arrayOf(
            "-y",
            "-i",
            finalVideo,
            "-i",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString() + "/logo.png",
            "-filter_complex",
            "[1][0]scale2ref=w='iw*25/100':h='ow/mdar'[wm][vid];[vid][wm]overlay=10:10",
            "-codec:a",
            "copy",
            location.absolutePath
        )
        FFmpeg.executeAsync(
            cmd
        ) { _, _ ->
            File(finalVideo).delete()
            val newFile = utripodFile("Utripod_Final_COMPRESSED.mp4")
            VideoCompressor.start(
                this,
                Uri.fromFile(location),
                null,
                newFile.absolutePath,
                null,
                object :CompressionListener{
                    override fun onCancelled() {

                    }

                    override fun onFailure(failureMessage: String) {
                        Log.d("COMPRESS",failureMessage)

                    }

                    override fun onProgress(percent: Float) {
                    }

                    override fun onStart() {
                    }

                    override fun onSuccess() {
                        runOnUiThread {
                            videoPath = newFile.absolutePath
                            simpleExoPlayer.addMediaItem(MediaItem.fromUri(newFile.absolutePath))
                            setViews()
                        }
                    }
                },
                Configuration(
                    quality = VideoQuality.HIGH,
                    frameRate = 24, /*Int, ignore, or null*/
                    false,
                )
            )

        }

    }

    private fun addMusic(myEditedVideo: String) {
        //trimming music
        val location = utripodFile("Utripod_${System.currentTimeMillis()}.mp4")
        val cmd: Array<String> = arrayOf(
            "-i",
            musicPath,
            "-ss",
            startTime,
            "-c",
            "copy",
            "-y",
            location.absolutePath
        )
        FFmpeg.executeAsync(
            cmd
        ) { _, _ ->
            //concatenating music with video
            val outputFile = utripodFile("Utripod_${System.currentTimeMillis()}.mp4")

            val finalCmd: Array<String> = arrayOf(
                "-i",
                myEditedVideo,
                "-i",
                location.absolutePath,
                outputFile.absolutePath
            )

            FFmpeg.executeAsync(
                finalCmd
            ) { _, _ ->
                File(location.absolutePath).delete()
                File(myEditedVideo).delete()
                //trimming video with music file
                uploadFile = utripodFile("Utripod_${System.currentTimeMillis()}.mp4")
                val command: Array<String> = arrayOf(
                    "-ss",
                    "0",
                    "-i",
                    outputFile.absolutePath,
                    "-to",
                    duration,
                    "-c",
                    "copy",
                    uploadFile!!.absolutePath
                )
                FFmpeg.executeAsync(
                    command
                ) { _, _ ->
                    File(outputFile.absolutePath).delete()
                    videoPath = uploadFile!!.absolutePath.toString()
                    addWaterMark(videoPath)
                }
            }
        }
    }

    private fun setViews() {
        simpleExoPlayer.prepare()
        binding.progressCircular.visibility = INVISIBLE
        binding.txtLoading.visibility = INVISIBLE
        binding.imgDone.visibility = VISIBLE
        binding.player.visibility = VISIBLE
    }

    override fun onStop() {
        simpleExoPlayer.stop()
        super.onStop()
    }

    override fun onPause() {
        simpleExoPlayer.playWhenReady = false
        super.onPause()
    }

    override fun onResume() {
        simpleExoPlayer.playWhenReady = true
        super.onResume()
    }

    override fun onDestroy() {
        simpleExoPlayer.stop()
        simpleExoPlayer.release()
        super.onDestroy()
    }
}