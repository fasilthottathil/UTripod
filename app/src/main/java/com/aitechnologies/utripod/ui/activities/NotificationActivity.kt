package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.adapters.NotificationAdapter
import com.aitechnologies.utripod.databinding.ActivityNotificationBinding
import com.aitechnologies.utripod.models.Notification
import com.aitechnologies.utripod.repository.NotificationRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.NotificationViewModel
import com.aitechnologies.utripod.ui.viewModels.NotificationViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.aitechnologies.utripod.uvis.activities.UvisSingleActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private val notificationAdapter by lazy { NotificationAdapter(this) }
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var notificationViewModelProvider: NotificationViewModelProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationViewModelProvider = NotificationViewModelProvider(
            NotificationRepository(application),
            UserRepository()
        )

        notificationViewModel = ViewModelProvider(
            this,
            notificationViewModelProvider
        )[NotificationViewModel::class.java]

        setupRecyclerView()

        notificationViewModel.getNotifications(getUsername())

        notificationViewModel.notification.observe(this, {
            hideLoading()
            val list: ArrayList<Notification> = arrayListOf()
            if (it.isNotEmpty()) {
                it.forEach { notification ->
                    var found = false
                    for (i in 0 until list.size) {
                        if (list[i].message == notification.message && list[i].id == notification.id) {
                            if (list[i].timestamp < notification.timestamp) {
                                list[i].timestamp = notification.timestamp
                            }
                            found = true
                        }
                    }
                    if (!found)
                        list.add(notification)
                }
                notificationAdapter.setData(list)
            } else {
                shortToast("No notifications")
            }
        })

        notificationViewModel.userProfile.observe(this, @ExperimentalCoroutinesApi {
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

        notificationAdapter.setOnPostNotificationClickListener {
            startActivity(
                Intent(this, PostSingleActivity::class.java)
                    .putExtra("id", it)
            )
        }

        notificationAdapter.setOnUvisNotificationClickListener {
            startActivity(
                Intent(this, UvisSingleActivity::class.java)
                    .putExtra("id", it)
            )
        }

        notificationAdapter.setOnUserNotificationClickListener {
            showProgress("Loading...", false)
            notificationViewModel.getUserProfile(it)
        }

        binding.imgBack.setOnClickListener { onBackPressed() }

    }

    private fun hideLoading() {
        binding.progressCircular.visibility = INVISIBLE
    }

    private fun setupRecyclerView() {
        binding.rvNotifications.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@NotificationActivity)
            adapter = notificationAdapter
        }
    }
}