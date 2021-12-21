package com.aitechnologies.utripod.util

import android.content.Context.MODE_PRIVATE

object Constants {
    const val MY_PREFERENCE = "MY_SHARED_PREFERENCE"
    const val PREFERENCE_MODE = MODE_PRIVATE
    const val IS_LOGIN = "IS_LOGIN"
    const val USERNAME = "USERNAME"
    const val PROFESSION = "PROFESSION"
    const val USER_ID = "USER_ID"
    const val PROFILE_URL = "PROFILE_URL"
    const val USERS = "users"
    const val BLOCKED_USERS = "blocked_users"
    const val POST_PROMOTIONS = "post_promotions"
    const val UVIS_PROMOTIONS = "uvis_promotions"
    const val POSTS = "posts"
    const val UVIS = "uvis"
    const val POSTS_HASH_TAGS = "posts_hash_tags"
    const val UVIS_HASH_TAGS = "uvis_hash_tags"
    const val POSTS_TAGS = "posts_tags"
    const val UVIS_TAGS = "uvis_tags"
    const val FOLLOWINGS = "followings"
    const val FOLLOWERS = "followers"
    const val SOCIAL_LINKS = "social_links"
    const val POST_COMMENTS = "post_comments"
    const val UVIS_COMMENTS = "uvis_comments"
    const val POST_COMMENTS_REPLY = "post_comments_replies"
    const val UVIS_COMMENT_REPLY = "uvis_comments_replies"
    const val POST_REPORTS = "post_reports"
    const val UVIS_REPORTS = "uvis_reports"
    const val MY_CHATS = "my_chats"
    const val MY_GROUPS = "my_groups"
    const val GROUPS = "groups"
    const val GROUP_CHATS = "group_chats"
    const val GROUP_MEMBERS = "group_members"
    const val PRIVATE_CHATS = "private_chats"
    const val MIN_BUFFER_DURATION = 2000
    const val MAX_BUFFER_DURATION = 5000
    const val MIN_PLAYBACK_START_BUFFER = 1500
    const val MIN_PLAYBACK_RESUME_BUFFER = 2000
    const val TRENDING_HASH_TAGS = "trending_hashtags"
    const val MY_NOTIFICATIONS = "my_notifications"
    const val CONTENT_TYPE = "application/json"
    const val fcmAPI = "https://fcm.googleapis.com/fcm/send"
    const val SERVER_KEY =
        "key=" + "AAAA8Xcn4lA:APA91bHJ8E7CQDYA5CMYXTHwSB73uM5dPtVp_1sHqUUlV0naOpk9IY3T-JPgCt_0lOlRDyinQtSls6ot5SGp8eZTkXsPUEfnml2iycPqyV0J0EvtYqcLQWSYCB1gbkkrpDAkH0cSGLXf"

    const val CHANNEL_ID = "UTripod Notification"
    const val CHANNEL_NAME = "UTripod notification updates"
    const val CHANNEL_ID_OFFLINE = "UTripod Offline"
    const val CHANNEL_NAME_OFFLINE = "UTripod offline notification"
    const val CONNECTION = "connection"
    const val SERVICE_RUNNING = "SERVICE_RUNNING"
}