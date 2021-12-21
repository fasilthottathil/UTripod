package com.aitechnologies.utripod.uvis.activities

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.databinding.ActivityMusicBinding
import com.aitechnologies.utripod.databinding.MusicItemBinding
import com.aitechnologies.utripod.models.Music
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.hideKeyboard
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.utripodFile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MusicActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMusicBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var mediaPlayer: MediaPlayer
    private var musicList: ArrayList<Music> = arrayListOf()
    private var selected = ""
    private var canPlay = true
    private var prepared = false
    private val rvAdapter by lazy { MusicAdapter(this) }
    private lateinit var firebaseStorage: FirebaseStorage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()



        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener {
            prepared = true
            if (canPlay) {
                binding.layoutMusic.visibility = VISIBLE
                it.start()
                binding.pgMusic.visibility = INVISIBLE
            }
        }

        binding.rvMusic.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MusicActivity)
            adapter = rvAdapter
        }


        loadMusics()

        binding.imgPlay.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            } else {
                if (prepared) {
                    mediaPlayer.start()
                }
            }
        }

        binding.imgClose.setOnClickListener {
            mediaPlayer.pause()
            mediaPlayer.reset()
            binding.layoutMusic.visibility = GONE
            binding.pgMusic.visibility = VISIBLE
        }

        binding.imgSearchClose.setOnClickListener {
            binding.edtSearch.text.clear()
            binding.layoutMusic.visibility = GONE
            binding.imgSearchClose.visibility = GONE
            binding.progressCircular.visibility = VISIBLE
            musicList.clear()
            rvAdapter.setData(musicList)
            loadMusics()
        }

        binding.edtSearch.setOnEditorActionListener { _, i, _ ->
            hideKeyboard(this, binding.root)
            if (i == EditorInfo.IME_ACTION_SEARCH && binding.edtSearch.text.isNotEmpty() && binding.edtSearch.text.isNotBlank()) {
                mediaPlayer.pause()
                mediaPlayer.reset()
                binding.layoutMusic.visibility = GONE
                searchMusic(binding.edtSearch.text.toString())
            }
            true
        }

        binding.imgBack.setOnClickListener { onBackPressed() }

    }

    private fun searchMusic(search: String) {
        binding.imgSearchClose.visibility = VISIBLE
        musicList.clear()
        rvAdapter.setData(musicList)
        binding.progressCircular.visibility = VISIBLE
        firebaseFirestore.collection("music")
            .orderBy("name")
            .startAt(search.uppercase())
            .endAt(search.lowercase() + "\uf8ff")
            .get()
            .addOnSuccessListener {
                binding.progressCircular.visibility = GONE
                if (it.isEmpty) {
                    shortToast("No musics found")
                } else {
                    for (doc in it) {
                        musicList.add(
                            Music(
                                doc.data["name"].toString(),
                                doc.data["url"].toString()
                            )
                        )
                    }
                    rvAdapter.setData(musicList)
                }
            }
    }

    private fun loadMusics() {
        firebaseFirestore.collection("music")
            .get()
            .addOnSuccessListener {
                binding.progressCircular.visibility = GONE
                if (!it.isEmpty) {
                    for (doc in it) {
                        musicList.add(
                            Music(
                                doc.data["name"].toString(),
                                doc.data["url"].toString()
                            )
                        )
                    }
                    rvAdapter.setData(musicList)
                }
            }
    }

    inner class MusicAdapter(val context: Context) :
        RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

        inner class MusicDiffUtil(
            private val oldList: List<Music>,
            private val newList: List<Music>
        ) : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return oldList.size
            }

            override fun getNewListSize(): Int {
                return newList.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].url == newList[newItemPosition].url
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
        }

        private var musicModel: ArrayList<Music> = arrayListOf()

        inner class ViewHolder(val binding: MusicItemBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                MusicItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.txtMusic.text = musicModel[position].name

            holder.binding.layout.setOnClickListener {

                if (selected == musicModel[position].url) {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                    } else {
                        mediaPlayer.start()
                    }
                } else {
                    binding.txtMusic.text = musicModel[position].name
                    selected = musicModel[position].url
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                    }
                    binding.pgMusic.visibility = VISIBLE
                    binding.layoutMusic.visibility = VISIBLE
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(musicModel[position].url)
                    mediaPlayer.prepareAsync()
                }
            }
            holder.binding.btnUse.setOnClickListener {
                showProgress("Setting...", false)
                val location = utripodFile("Utripod_music${System.currentTimeMillis()}.mp3")
                firebaseStorage.getReferenceFromUrl(musicModel[position].url)
                    .getFile(location)
                    .addOnSuccessListener {
                        dismissProgress()
                        val intent = Intent()
                        intent.putExtra("music_uri", location.absolutePath.toString())
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                    .addOnFailureListener {
                        dismissProgress()
                        shortToast("An error occurred")
                    }
            }
        }

        override fun getItemCount(): Int {
            return musicModel.size
        }

        override fun getItemViewType(position: Int): Int {
            return (position)
        }

        fun setData(newList: List<Music>) {
            val diffUtil = MusicDiffUtil(musicModel, newList)
            val diffResult = DiffUtil.calculateDiff(diffUtil)
            musicModel.clear()
            musicModel.addAll(newList)
            diffResult.dispatchUpdatesTo(this)
        }
    }

    override fun onPause() {
        canPlay = false
        mediaPlayer.pause()
        super.onPause()
    }

    override fun onResume() {
        canPlay = true
        if (prepared)
            mediaPlayer.start()
        super.onResume()
    }

    override fun onDestroy() {
        mediaPlayer.release()
        AppUtil.releaseUtils()
        super.onDestroy()
    }
}