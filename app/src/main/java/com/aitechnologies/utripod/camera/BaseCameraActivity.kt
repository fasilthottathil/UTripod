package com.aitechnologies.utripod.camera

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.View.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.camera.widget.SampleCameraGLView
import com.aitechnologies.utripod.databinding.RowWhiteTextBinding
import com.aitechnologies.utripod.util.AppUtil.Companion.milliSecondsToTimer
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.uvis.activities.MusicActivity
import com.aitechnologies.utripod.uvis.activities.VideoEditorActivity
import com.daasuu.gpuv.camerarecorder.CameraRecordListener
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorder
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorderBuilder
import com.daasuu.gpuv.camerarecorder.LensFacing
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gowtham.library.utils.TrimType
import com.gowtham.library.utils.TrimVideo
import java.io.File
import java.util.*


open class BaseCameraActivity : AppCompatActivity() {
    private var gpuCameraRecorder: GPUCameraRecorder? = null
    private var lensFacing = LensFacing.BACK

    @JvmField
    protected var cameraWidth = 1280

    @JvmField
    protected var cameraHeight = 720

    @JvmField
    protected var videoWidth = 720

    @JvmField
    protected var videoHeight = 720
    private var sampleGLView: SampleCameraGLView? = null
    private var filepath: String? = null
    private var recordBtn: ImageView? = null
    private var toggleClick = false
    private var rvEffects: RecyclerView? = null
    private var isRecording = false
    private var layoutFeature: LinearLayout? = null
    private var imgEffect: ImageButton? = null
    private var imgGallery: ImageButton? = null
    private var imgTime: ImageButton? = null
    private var imgClose: ImageButton? = null
    private var imgTimer: ImageButton? = null
    private var imgMusic: ImageButton? = null
    private var imgEditMusic: ImageButton? = null
    private var imgClear: ImageView? = null
    private var imgHide: ImageView? = null
    private var layoutEffects: ConstraintLayout? = null
    private val filterTypes: List<FilterType> = FilterType.createFilterList()

    private lateinit var mediaPlayer: MediaPlayer
    private var startTime = 0
    private var duration = 0
    private lateinit var seekHandler: Handler
    private var cancelled = false
    private var time = 15000
    private var timer = 0
    private var videoLength = 15
    private var musicUri = ""
    private lateinit var videoSelectResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var trimResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var musicResultLauncher: ActivityResultLauncher<Intent>
    private var txtCount: TextView? = null
    private var countDownTimer: CountDownTimer? = null

    @SuppressLint("SetTextI18n")
    protected fun onCreateActivity() {

        window.statusBarColor = Color.parseColor("#010101")
        window.navigationBarColor = Color.parseColor("#010101")

        layoutFeature = findViewById(R.id.layoutFeature)
        recordBtn = findViewById(R.id.btn_record)
        imgEffect = findViewById(R.id.imgEffect)
        imgGallery = findViewById(R.id.imgGallery)
        imgTime = findViewById(R.id.imgTime)
        imgClose = findViewById(R.id.imgClose)
        imgTimer = findViewById(R.id.imgTimer)
        imgMusic = findViewById(R.id.imgMusic)
        imgEditMusic = findViewById(R.id.imgEditMusic)
        imgClear = findViewById(R.id.imgClear)
        imgHide = findViewById(R.id.imgHide)
        rvEffects = findViewById(R.id.rvEffects)
        layoutEffects = findViewById(R.id.layoutEffects)
        txtCount = findViewById(R.id.txtCount)

        if (intent.getBooleanExtra("isFromAudio", false)) {
            musicUri = intent.getStringExtra("audio").toString()
        }

        recordBtn!!.setOnClickListener {
            if (isRecording) {
                gpuCameraRecorder!!.stop()
                recordBtn!!.setImageResource(R.drawable.record_selector)
            } else {
                startRecording()
            }
            isRecording = !isRecording
        }
        findViewById<View>(R.id.btn_flash).setOnClickListener {
            if (gpuCameraRecorder != null && gpuCameraRecorder!!.isFlashSupport) {
                gpuCameraRecorder!!.switchFlashMode()
                gpuCameraRecorder!!.changeAutoFocus()
            }
        }
        findViewById<View>(R.id.btn_switch_camera).setOnClickListener {
            releaseCamera()
            lensFacing = if (lensFacing == LensFacing.BACK) {
                LensFacing.FRONT
            } else {
                LensFacing.BACK
            }
            toggleClick = true
        }

        imgClear!!.setOnClickListener {
            if (gpuCameraRecorder != null) {
                gpuCameraRecorder!!.setFilter(
                    FilterType.createGlFilter(
                        filterTypes[0],
                        applicationContext
                    )
                )
            }

        }
        imgClose!!.setOnClickListener { onBackPressed() }
        imgHide!!.setOnClickListener {
            layoutEffects!!.visibility = INVISIBLE
        }
        imgEffect!!.setOnClickListener {
            layoutEffects!!.visibility = VISIBLE
        }
        imgGallery!!.setOnClickListener {
            videoSelectResultLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).setType("video/*"))
        }


        rvEffects!!.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(this@BaseCameraActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = EffectAdapter(filterTypes)
        }


        mediaPlayer = MediaPlayer()
        seekHandler = Handler(Looper.myLooper()!!)


        musicResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    imgEditMusic!!.visibility = VISIBLE
                    musicUri = it.data!!.extras!!.getString("music_uri").toString()
                }
            }


        videoSelectResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    TrimVideo.activity(it.data!!.data.toString())
                        .setTrimType(TrimType.MIN_MAX_DURATION)
                        .setHideSeekBar(true)
                        .setMinToMax(3, 30)
                        .setFileName("UTripod_${System.currentTimeMillis()}")
                        .start(this, trimResultLauncher)
                }
            }

        trimResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.data != null) {

                    startActivity(
                        Intent(
                            this,
                            VideoEditorActivity::class.java
                        )
                            .putExtra(
                                "video_path",
                                TrimVideo.getTrimmedVideoPath(it.data).toString()
                            )
                            .putExtra("music_uri", musicUri)
                            .putExtra("start_time", startTime)
                            .putExtra("duartion", time)
                    )
                    finish()
                }
            }

        imgMusic!!.setOnClickListener {
            if (musicUri.isEmpty()) {
                musicResultLauncher.launch(Intent(this, MusicActivity::class.java))
            } else {
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.apply {
                    setMessage("Do you want to remove music?")
                    setNegativeButton("No") { dialog, _ ->
                        dialog.cancel()
                    }
                    setPositiveButton("Yes") { dialog, _ ->
                        imgEditMusic!!.visibility = INVISIBLE
                        musicUri = ""
                        dialog.cancel()
                    }
                }.create().show()
            }
        }

        imgEditMusic!!.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this)
            val musicEditView = inflate(this, R.layout.music_edit_layout, null)
            val waveFormSeekBar: SeekBar = musicEditView.findViewById(R.id.waveformSeekBar)
            val selectedTimeText: TextView = musicEditView.findViewById(R.id.txtMusic)
            val txtSave: TextView = musicEditView.findViewById(R.id.txtSave)
            bottomSheetDialog.setContentView(musicEditView)
            bottomSheetDialog.show()
            bottomSheetDialog.setCancelable(false)

            selectedTimeText.text = "Start time : " + milliSecondsToTimer(startTime.toLong())

            mediaPlayer.reset()
            mediaPlayer.setDataSource(musicUri)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                mediaPlayer.start()
                mediaPlayer.seekTo(startTime)
                waveFormSeekBar.max = mediaPlayer.duration
                seekHandler.postDelayed(updateSeekBar(waveFormSeekBar), 15)
            }

            waveFormSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer.seekTo(p1)
                        startTime = p1
                        selectedTimeText.text =
                            "Start time : " + milliSecondsToTimer(mediaPlayer.currentPosition.toLong())
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }
            })
            txtSave.setOnClickListener {
                duration = (((mediaPlayer.duration - mediaPlayer.currentPosition) / 1000) % 60)
                if (duration < 5) {
                    shortToast("Length must be at least 5 seconds")
                } else {
                    cancelled = true
                    mediaPlayer.stop()
                    mediaPlayer.reset()
                    seekHandler.removeCallbacks(updateSeekBar(waveFormSeekBar))
                    bottomSheetDialog.dismiss()
                }

            }
        }

        imgTime!!.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
            if (time == 15000) {
                videoLength = 30
                time = 30000
                txtCount!!.text = HtmlCompat.fromHtml("30s", 0)
                txtCount!!.startAnimation(animation)
            } else {
                videoLength = 15
                time = 15000
                txtCount!!.text = HtmlCompat.fromHtml("15s", 0)
                txtCount!!.startAnimation(animation)
            }
        }

        imgTimer!!.setOnClickListener {
            timer = 5
            val bottomSheetDialog = BottomSheetDialog(this)
            val view = inflate(this, R.layout.timer_bottom_sheet, null)
            val cv1: CardView = view.findViewById(R.id.cv1)
            val cv2: CardView = view.findViewById(R.id.cv2)
            val cv3: CardView = view.findViewById(R.id.cv3)
            val btnContinue: Button = view.findViewById(R.id.btnStart)
            val imgClose: ImageView = view.findViewById(R.id.imgClose)

            val clickListener = OnClickListener {
                cv1.setCardBackgroundColor(Color.parseColor("#2D2D2D"))
                cv2.setCardBackgroundColor(Color.parseColor("#2D2D2D"))
                cv3.setCardBackgroundColor(Color.parseColor("#2D2D2D"))

                when (it.id) {
                    R.id.cv1 -> {
                        timer = 5
                        cv1.setCardBackgroundColor(Color.parseColor("#186BFE"))
                    }
                    R.id.cv2 -> {
                        timer = 10
                        cv2.setCardBackgroundColor(Color.parseColor("#186BFE"))
                    }
                    R.id.cv3 -> {
                        timer = 15
                        cv3.setCardBackgroundColor(Color.parseColor("#186BFE"))
                    }
                }
            }

            cv1.setOnClickListener(clickListener)
            cv2.setOnClickListener(clickListener)
            cv3.setOnClickListener(clickListener)

            bottomSheetDialog.apply {
                setContentView(view)
                setCancelable(false)
            }.show()

            btnContinue.setOnClickListener {
                imgClose.visibility = INVISIBLE
                btnContinue.text = "cancel"
                startTimer(btnContinue, bottomSheetDialog)
            }

            imgClose.setOnClickListener {
                bottomSheetDialog.cancel()
            }
        }

    }

    private fun startRecording() {
        filepath =
            utripodFile("Utripod_recorded_${System.currentTimeMillis()}.mp4").absolutePath
        if (musicUri.isNotEmpty()) {
           try {
               mediaPlayer.reset()
               mediaPlayer.setDataSource(musicUri)
               mediaPlayer.prepareAsync()
               mediaPlayer.setOnPreparedListener {
                   mediaPlayer.start()
                   mediaPlayer.seekTo(startTime)
               }
           }catch (ignored:Exception){
               mediaPlayer.reset()
               mediaPlayer.setDataSource(File(musicUri).absolutePath)
               mediaPlayer.prepareAsync()
               mediaPlayer.setOnPreparedListener {
                   mediaPlayer.start()
                   mediaPlayer.seekTo(startTime)
               }
           }
        }

        layoutFeature!!.visibility = GONE
        imgEffect!!.visibility = INVISIBLE
        imgGallery!!.visibility = INVISIBLE
        gpuCameraRecorder!!.start(filepath)
        recordBtn!!.setImageResource(R.drawable.record_end_btn)
        val progressBar: ProgressBar = findViewById(R.id.progress_horizontal)
        progressBar.max = time
        progressBar.visibility = VISIBLE
        countDownTimer = object : CountDownTimer(time.toLong(), 1000) {
            override fun onTick(p0: Long) {
                progressBar.progress = p0.toInt()
            }

            override fun onFinish() {
                progressBar.visibility = INVISIBLE
                gpuCameraRecorder!!.stop()
            }
        }.start()
    }

    private fun updateSeekBar(seekbar: SeekBar): Runnable = object : Runnable {
        override fun run() {
            if (!cancelled) {
                val currentDuration = mediaPlayer.currentPosition.toLong()
                // Updating progress bar
                seekbar.progress = currentDuration.toInt()
                // Call this thread again after 15 milliseconds => ~ 1000/60fps
                seekHandler.postDelayed(this, 15)
            }

        }
    }

    private fun startTimer(btnContinue: Button, bottomSheetDialog: BottomSheetDialog) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        val txtCount: TextView = findViewById(R.id.txtCount)
        var count = 0
        val milliInFuture = (timer * 1000).toLong()
        val countDownTimer = object : CountDownTimer(milliInFuture, 1000) {
            override fun onTick(p0: Long) {
                count += 1
                txtCount.text = count.toString()
                txtCount.startAnimation(animation)
            }

            override fun onFinish() {
                bottomSheetDialog.cancel()
                startRecording()
            }
        }.start()

        btnContinue.setOnClickListener {
            bottomSheetDialog.cancel()
            countDownTimer.cancel()
        }
    }

    override fun onResume() {
        super.onResume()
        setUpCamera()
    }

    override fun onStop() {
        super.onStop()
        releaseCamera()
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
        super.onDestroy()
    }

    private fun releaseCamera() {
        if (sampleGLView != null) {
            sampleGLView!!.onPause()
        }
        if (gpuCameraRecorder != null) {
            gpuCameraRecorder!!.stop()
            gpuCameraRecorder!!.release()
            gpuCameraRecorder = null
        }
        if (sampleGLView != null) {
            (findViewById<View>(R.id.wrap_view) as FrameLayout).removeView(sampleGLView)
            sampleGLView = null
        }
    }

    private fun setUpCameraView() {
        runOnUiThread {
            val frameLayout = findViewById<FrameLayout>(R.id.wrap_view)
            frameLayout.removeAllViews()
            sampleGLView = null
            sampleGLView = SampleCameraGLView(applicationContext)
            sampleGLView!!.setTouchListener { event: MotionEvent, width: Int, height: Int ->
                if (gpuCameraRecorder == null) return@setTouchListener
                gpuCameraRecorder!!.changeManualFocusPoint(event.x, event.y, width, height)
            }
            frameLayout.addView(sampleGLView)
        }
    }

    private fun setUpCamera() {
        setUpCameraView()
        gpuCameraRecorder = GPUCameraRecorderBuilder(this, sampleGLView) //.recordNoFilter(true)
            .cameraRecordListener(object : CameraRecordListener {
                override fun onGetFlashSupport(flashSupport: Boolean) {
                    runOnUiThread { findViewById<View>(R.id.btn_flash).isEnabled = flashSupport }
                }

                override fun onRecordComplete() {
                    countDownTimer!!.cancel()
                    startActivity(
                        Intent(
                            this@BaseCameraActivity,
                            VideoEditorActivity::class.java
                        )
                            .putExtra("video_path", filepath.toString())
                            .putExtra("music_uri", musicUri)
                            .putExtra("start_time", startTime)
                            .putExtra("duartion", time)
                    )
                    finish()
//                    exportMp4ToGallery(applicationContext, filepath)
                }

                override fun onRecordStart() {
                    runOnUiThread {
                        layoutFeature!!.visibility = GONE
                        imgEffect!!.visibility = INVISIBLE
                        imgGallery!!.visibility = INVISIBLE
                    }
                }

                override fun onError(exception: Exception) {
                }

                override fun onCameraThreadFinish() {
                    if (toggleClick) {
                        runOnUiThread { setUpCamera() }
                    }
                    toggleClick = false
                }

                override fun onVideoFileReady() {}
            })
            .videoSize(videoWidth, videoHeight)
            .cameraSize(cameraWidth, cameraHeight)
            .lensFacing(lensFacing)
            .build()
    }


    private fun utripodFile(fileName: String): File {
        val myDir = getExternalFilesDir(null)
        val folder = File(myDir, "UTripod")
        if (!folder.mkdirs()) {
            folder.mkdirs()
        }
        return File(folder.absolutePath, fileName)

    }

    inner class EffectAdapter(private val filterList: List<FilterType>) :
        RecyclerView.Adapter<EffectAdapter.ViewHolder>() {
        inner class ViewHolder(val binding: RowWhiteTextBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                RowWhiteTextBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.label.text = filterList[position].name
            holder.binding.label.setOnClickListener {
                if (gpuCameraRecorder != null) {
                    gpuCameraRecorder!!.setFilter(
                        FilterType.createGlFilter(
                            filterTypes[position],
                            applicationContext
                        )
                    )
                }
            }
        }

        override fun getItemCount(): Int {
            return filterList.size
        }

        override fun getItemViewType(position: Int): Int {
            return (position)
        }
    }

}