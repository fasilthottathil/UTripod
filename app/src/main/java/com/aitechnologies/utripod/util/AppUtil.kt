package com.aitechnologies.utripod.util

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Context.INPUT_METHOD_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Environment
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.interfaces.ProgressListener
import com.aitechnologies.utripod.interfaces.UploadFileResponse
import com.aitechnologies.utripod.models.Posts
import com.aitechnologies.utripod.models.UvisModel
import com.aitechnologies.utripod.util.Constants.MAX_BUFFER_DURATION
import com.aitechnologies.utripod.util.Constants.MIN_BUFFER_DURATION
import com.aitechnologies.utripod.util.Constants.MIN_PLAYBACK_RESUME_BUFFER
import com.aitechnologies.utripod.util.Constants.MIN_PLAYBACK_START_BUFFER
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File


class AppUtil {

    companion object {

        private var firebaseStorage: FirebaseStorage? = null
        private var alertDialog: AlertDialog? = null
        private lateinit var onBottomSheetItemClickListener:((Int,Posts,Boolean)->Unit)
        private lateinit var onBottomSheetItemUvisClickListener: ((Int, UvisModel, Boolean) -> Unit)
        private var loadControl: LoadControl? = null

        private const val secondMillis = 1000
        private const val minuteMillis = 60 * secondMillis
        private const val hourMillis = 60 * minuteMillis
        private const val daysMillis = 24 * hourMillis

        suspend fun anonymousAuthentication(firebaseAuth: FirebaseAuth): AuthResult? {
            return try {
                val data = firebaseAuth
                    .signInAnonymously()
                    .await()
                data
            } catch (e: Exception) {
                null
            }
        }

        fun Context.isConnected(): Boolean {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val nc = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            return when {
                nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }

        fun Context.shortToast(message: String) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        fun getTimeAgo(timeStamp: Long): CharSequence? {
            var time = timeStamp
            if (time < 1000000000000L) {
                time *= 1000
            }

            val now = System.currentTimeMillis()
            if (time > now || time <= 0) {
                return null
            }


            val diff: Long = now - time
            return when {
                diff < minuteMillis -> {
                    "just now"
                }
                diff < 2 * minuteMillis -> {
                    "1 min ago"
                }
                diff < 50 * minuteMillis -> {
                    " ${diff / minuteMillis} min ago"
                }
                diff < 90 * minuteMillis -> {
                    "1 hr ago"
                }
                diff < 24 * hourMillis -> {
                    " ${diff / hourMillis} hr ago"
                }
                diff < 48 * hourMillis -> {
                    "yesterday"
                }
                else -> {
                    " ${diff / daysMillis} d ago"
                }
            }
        }

        fun uploadFile(
            file: Uri,
            name: String,
            location: String,
            uploadFileResponse: UploadFileResponse
        ) {
            if (firebaseStorage == null)
                getStorageInstance()
            val storage = firebaseStorage!!.reference.child("$location/$name")
            storage.putFile(file)
                .addOnProgressListener {
                    uploadFileResponse.onProgress(((100.0 * it.bytesTransferred) / it.totalByteCount).toInt())
                }
                .addOnSuccessListener {
                    storage.downloadUrl.addOnSuccessListener { url ->
                        uploadFileResponse.onSuccess(url.toString())
                    }
                        .addOnFailureListener {
                            uploadFileResponse.onFailure(it.message.toString())
                        }
                }
                .addOnFailureListener {
                    uploadFileResponse.onFailure(it.message.toString())
                }

        }

        fun Context.showProgress(
            message: String,
            isCancellable: Boolean,
            progressListener: ProgressListener? = null
        ) {
            if (alertDialog != null){
                if (alertDialog!!.isShowing){
                    return
                }
            }
            val view = View.inflate(this, R.layout.progress_view, null)
            view.findViewById<TextView>(R.id.txtMessage).text = message
            alertDialog = AlertDialog.Builder(this)
                .apply {
                    setCancelable(isCancellable)
                    setView(view)
                    if (isCancellable) {
                        setNegativeButton("cancel") { d, _ ->
                            d.cancel()
                        }
                    }
                    setOnDismissListener { progressListener?.onDismiss() }
                }.create()

            alertDialog?.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL)

            alertDialog?.show()
        }

        fun dismissProgress() = alertDialog?.dismiss()

        fun Context.showPostBottomSheetDialog(
            type: Int,
            posts: Posts,
            canShowBlock: Boolean? = null
        ) {
            val view = View.inflate(this, R.layout.post_bottom_sheet, null)
            val share = view.findViewById<LinearLayoutCompat>(R.id.share)
            val report = view.findViewById<LinearLayoutCompat>(R.id.report)
            val delete = view.findViewById<LinearLayoutCompat>(R.id.delete)
            val promote = view.findViewById<LinearLayoutCompat>(R.id.promote)
            val edit = view.findViewById<LinearLayoutCompat>(R.id.edit)
            val block = view.findViewById<LinearLayoutCompat>(R.id.blocked)

            delete.visibility = GONE
            promote.visibility = GONE
            edit.visibility = GONE
            report.visibility = GONE
            block.visibility = GONE

            val isPromotion = when (type) {
                3 -> true
                4 -> true
                5 -> true
                else -> false
            }

            val bottomSheetDialog = BottomSheetDialog(this, R.style.bottom_sheet_dialog_theme)
            bottomSheetDialog.setContentView(view)
            when (type) {
                0 -> {
                    delete.visibility = VISIBLE
                    promote.visibility = VISIBLE
                    edit.visibility = VISIBLE
                }
                1 -> {
                    delete.visibility = VISIBLE
                    edit.visibility = VISIBLE
                }
                else -> {
                    if (canShowBlock != null) {
                        if (canShowBlock)
                            block.visibility = VISIBLE
                    } else {
                        block.visibility = VISIBLE
                    }
                    report.visibility = VISIBLE
                }
            }
            bottomSheetDialog.setCanceledOnTouchOutside(true)
            bottomSheetDialog.show()

            share.setOnClickListener {
                bottomSheetDialog.dismiss()
                onBottomSheetItemClickListener(0, posts, isPromotion)
            }
            report.setOnClickListener {
                bottomSheetDialog.dismiss()
                onBottomSheetItemClickListener(1, posts, isPromotion)
            }
            delete.setOnClickListener {
                bottomSheetDialog.dismiss()
                AlertDialog.Builder(this)
                    .apply {
                        setMessage("Do you want to delete?")
                        setNegativeButton("No") { d, _ -> d.cancel() }
                        setPositiveButton("Yes") { d, _ ->
                            d.cancel()
                            onBottomSheetItemClickListener.invoke(2, posts, isPromotion)
                        }
                    }.create().show()
            }
            promote.setOnClickListener {
                bottomSheetDialog.dismiss()
                onBottomSheetItemClickListener(3, posts, isPromotion)
            }
            edit.setOnClickListener {
                bottomSheetDialog.dismiss()
                onBottomSheetItemClickListener(4, posts, isPromotion)
            }
            block.setOnClickListener {
                bottomSheetDialog.dismiss()
                onBottomSheetItemClickListener(5, posts, isPromotion)
            }
        }

        fun Context.showUvisBottomSheetDialog(type: Int, uvisModel: UvisModel) {
            val view = View.inflate(this, R.layout.post_bottom_sheet, null)
            val share = view.findViewById<LinearLayoutCompat>(R.id.share)
            val report = view.findViewById<LinearLayoutCompat>(R.id.report)
            val delete = view.findViewById<LinearLayoutCompat>(R.id.delete)
            val promote = view.findViewById<LinearLayoutCompat>(R.id.promote)
            val edit = view.findViewById<LinearLayoutCompat>(R.id.edit)
            val download = view.findViewById<LinearLayoutCompat>(R.id.download)

            delete.visibility = GONE
            promote.visibility = GONE
            edit.visibility = GONE
            report.visibility = GONE
            download.visibility = VISIBLE

            val isPromotion = type == 1

            val bottomSheetDialog = BottomSheetDialog(this, R.style.bottom_sheet_dialog_theme)
            bottomSheetDialog.setContentView(view)
            when (type) {
                0 -> {
                    delete.visibility = VISIBLE
                    promote.visibility = VISIBLE
                    edit.visibility = VISIBLE
                }
                1 -> {
                    delete.visibility = VISIBLE
//                    edit.visibility = VISIBLE
                }
                2 -> {
                    report.visibility = VISIBLE
                }
            }
            bottomSheetDialog.setCanceledOnTouchOutside(true)
            bottomSheetDialog.show()

            share.setOnClickListener {
                bottomSheetDialog.dismiss()
                onBottomSheetItemUvisClickListener(0, uvisModel, isPromotion)
            }
            report.setOnClickListener {
                bottomSheetDialog.dismiss()
                onBottomSheetItemUvisClickListener(1, uvisModel, isPromotion)
            }
            delete.setOnClickListener {
                bottomSheetDialog.dismiss()
                AlertDialog.Builder(this)
                    .apply {
                        setMessage("Do you want to delete?")
                        setNegativeButton("No") { d, _ -> d.cancel() }
                        setPositiveButton("Yes") { d, _ ->
                            d.cancel()
                            onBottomSheetItemUvisClickListener(2, uvisModel, isPromotion)
                        }
                    }.create().show()
            }
            promote.setOnClickListener {
                bottomSheetDialog.dismiss()
                onBottomSheetItemUvisClickListener(3, uvisModel, isPromotion)
            }
            edit.setOnClickListener {
                bottomSheetDialog.dismiss()
                onBottomSheetItemUvisClickListener(4, uvisModel, isPromotion)
            }
            download.setOnClickListener {
                bottomSheetDialog.dismiss()
                onBottomSheetItemUvisClickListener(5, uvisModel, isPromotion)
            }
        }


        fun onClickBottomSheetItem(listener: (Int, Posts, Boolean) -> Unit) {
            onBottomSheetItemClickListener = listener
        }

        fun onClickBottomSheetItemUvis(listener: (Int, UvisModel, Boolean) -> Unit) {
            onBottomSheetItemUvisClickListener = listener
        }

        private fun getStorageInstance() {
            firebaseStorage = FirebaseStorage.getInstance()
        }

        fun getLoadControl(): LoadControl {
            if (loadControl == null) {
                loadControl = DefaultLoadControl.Builder()
                    .setAllocator(DefaultAllocator(true, 16))
                    .setBufferDurationsMs(
                        MIN_BUFFER_DURATION,
                        MAX_BUFFER_DURATION,
                        MIN_PLAYBACK_START_BUFFER,
                        MIN_PLAYBACK_RESUME_BUFFER
                    )
                    .setTargetBufferBytes(-1)
                    .setPrioritizeTimeOverSizeThresholds(true)
                    .build()
            }
            return loadControl!!
        }

        fun milliSecondsToTimer(milliseconds: Long): String {
            var finalTimerString = ""
            val secondsString: String

            // Convert total duration into time
            val hours = (milliseconds / (1000 * 60 * 60)).toInt()
            val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
            val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
            // Add hours if there
            if (hours > 0) {
                finalTimerString = "$hours:"
            }

            // Prepending 0 to seconds if it is one digit
            secondsString = if (seconds < 10) {
                "0$seconds"
            } else {
                "" + seconds
            }
            finalTimerString = "$finalTimerString$minutes:$secondsString"

            // return timer string
            return finalTimerString
        }

        fun hideKeyboard(context: Context, view: View) {
            try {
                val inputMethodManager =
                    context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun downloadFile(
            context: Context,
            location: String
        ) {
            val downloadManager: DownloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(location)
            val request: DownloadManager.Request = DownloadManager.Request(uri)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "${System.currentTimeMillis()}.mp4"
            )
            downloadManager.enqueue(request)
            context.shortToast("Downloading started")
        }

        fun Context.utripodFile(fileName: String): File {
            return try {
                val myDir = getExternalFilesDir(null)
                val folder = File(myDir, "UTripod")
                if (!folder.mkdirs()) {
                    folder.mkdirs()
                }
                File(folder.absolutePath, fileName)
            } catch (e: Exception) {
                shortToast("Error with file creation")
                File("")
            }
        }

        fun releaseUtils(){
            alertDialog = null
//            onBottomSheetItemClickListener = null
//            onBottomSheetItemClickListener = null
        }

    }
}