package com.aitechnologies.utripod.uvis.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aitechnologies.utripod.camera.PortraitCameraActivity
import com.aitechnologies.utripod.databinding.ActivityAudioBinding
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.arthenica.mobileffmpeg.FFmpeg
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class AudioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAudioBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private var isCancelled = false
    private var isPrepared = false
    private var downloaded = false
    private var mp3File = ""
    private val handler = Handler(Looper.myLooper()!!)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        binding.txtUsername.text = intent.getStringExtra("name")
        Glide.with(applicationContext)
            .load(intent.getStringExtra("profileUrl"))
            .into(binding.imgProfile)

        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(intent.getStringExtra("postUrl").toString())
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            isPrepared = true
            binding.timeSeekBar.max = it.duration
            mediaPlayer.start()
            mediaPlayer.isLooping = true
            updateSeekBar()
        }

        binding.timeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (isPrepared) {
                        mediaPlayer.seekTo(p1)
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        binding.cvUse.setOnClickListener {
            if (!downloaded) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    showProgress("Loading...", false)
                    val localFile = utripodFile("UTripod_${System.currentTimeMillis()}.mp4")
                    firebaseStorage.getReferenceFromUrl(intent.getStringExtra("postUrl").toString())
                        .getFile(localFile)
                        .addOnSuccessListener {
                            downloaded = true
                            mp3File =
                                utripodFile("UTripod__download_music${System.currentTimeMillis()}.mp4").absolutePath
                            val complexCommand = arrayOf(
                                "-y",
                                "-i",
                                localFile.absolutePath,
                                "-vn",
                                "-ar",
                                "44100",
                                "-ac",
                                "2",
                                "-b:a",
                                "256k",
                                "-f",
                                "mp4",
                                mp3File
                            )
                            FFmpeg.executeAsync(complexCommand) {_,_->
                                File(localFile.absolutePath).delete()
                                dismissProgress()
                                permissions()
                            }
                        }
                } else {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                }
            } else {
                permissions()
            }

        }

        binding.imgBack.setOnClickListener { onBackPressed() }

    }

    private fun permissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1
            )
        } else {
            // Permission has already been granted
            startCamera()
        }
    }


    private fun updateSeekBar() {
        if (!isCancelled) {
            handler.postDelayed({
                binding.timeSeekBar.progress = mediaPlayer.currentPosition
                updateSeekBar()
            }, 15)
        }
    }

    private fun utripodFile(fileName: String): File {
        return try {
            val myDir = getExternalFilesDir(null)
            val folder = File(myDir, "UTripod")
            if (!folder.mkdirs()) {
                folder.mkdirs()
            }
            File(folder.absolutePath, fileName)
        } catch (e: Exception) {
            shortToast("Error with file creation")
            onBackPressed()
            File("")
        }
    }

    override fun onPause() {
        isCancelled = true
        mediaPlayer.pause()
        super.onPause()
    }

    override fun onResume() {
        isCancelled = false
        if (isPrepared)
            mediaPlayer.start()
        super.onResume()
    }

    override fun onDestroy() {
        isCancelled = true
        handler.removeCallbacks {}
        mediaPlayer.stop()
        mediaPlayer.release()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shortToast("Permission granted")
            } else {
                shortToast("Permission denied")
            }
        } else {
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return  // no permission
                }
            }
            startCamera()
        }

    }

    private fun startCamera() {
        startActivity(
            Intent(this, PortraitCameraActivity::class.java)
                .putExtra("isFromAudio", true)
                .putExtra("audio", mp3File)
        )
        finish()
    }
}