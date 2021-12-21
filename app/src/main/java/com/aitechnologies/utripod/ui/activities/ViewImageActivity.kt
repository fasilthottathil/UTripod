package com.aitechnologies.utripod.ui.activities

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View.INVISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.aitechnologies.utripod.databinding.ActivityViewImageBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class ViewImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(applicationContext)
            .load(intent.getStringExtra("image"))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressCircular.visibility = INVISIBLE
                    binding.imgImage.setImageDrawable(resource)
                    return true
                }
            })
            .into(binding.imgImage)


        binding.imgBack.setOnClickListener { onBackPressed() }

    }
}