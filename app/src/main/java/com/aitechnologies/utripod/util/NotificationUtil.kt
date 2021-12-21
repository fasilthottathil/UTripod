package com.aitechnologies.utripod.util

import android.app.Application
import android.content.Context
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.Constants.CONTENT_TYPE
import com.aitechnologies.utripod.util.Constants.SERVER_KEY
import com.aitechnologies.utripod.util.Constants.fcmAPI
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class NotificationUtil {

    companion object {

        private val firebaseMessaging by lazy { FirebaseMessaging.getInstance() }
        private var requestQueue: RequestQueue? = null

        fun subscribeToTopics(application: Application) {
            firebaseMessaging.subscribeToTopic(application.getUsername())
        }

        fun unsubscribeFromTopics(application: Application) {
            firebaseMessaging.unsubscribeFromTopic(application.getUsername())
        }

        fun Context.sendNotification(
            topic: String,
            message: String
        ) {
            val notification = JSONObject()
            val notificationBody = JSONObject()
            try {
                notificationBody.put("message", message)
                notification.put("to", "/topics/$topic")
                notification.put("data", notificationBody)
            } catch (e: JSONException) {
            }
            val jsonObjectRequest: JsonObjectRequest =
                object : JsonObjectRequest(Method.POST, fcmAPI, notification,
                    Response.Listener {
                        Timber.tag("Notification:Success").d(it.toString())
                    },
                    Response.ErrorListener {
                        Timber.tag("Notification:Error").d(it.message.toString())
                    }) {
                    override fun getHeaders(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["Authorization"] = SERVER_KEY
                        params["Content-Type"] = CONTENT_TYPE
                        return params
                    }
                }
            getRequestQueue(this).add(jsonObjectRequest)
        }

        private fun getRequestQueue(context: Context): RequestQueue {
            if (requestQueue == null)
                requestQueue = Volley.newRequestQueue(context)
            return requestQueue!!
        }

    }
}