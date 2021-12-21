package com.aitechnologies.utripod.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.text.HtmlCompat
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.databinding.ActivityPrivacyAndTermsBinding

class PrivacyAndTermsActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPrivacyAndTermsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyAndTermsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtPrivacy.text = HtmlCompat.fromHtml(
            getString(R.string.privacy_text),
            0
        )

        binding.txtTerms.text = HtmlCompat.fromHtml(
            getString(R.string.terms_text),
            0
        )


    }
}