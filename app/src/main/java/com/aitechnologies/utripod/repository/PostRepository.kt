package com.aitechnologies.utripod.repository

import android.app.Application
import android.util.Log
import com.aitechnologies.utripod.models.*
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.Constants.FOLLOWINGS
import com.aitechnologies.utripod.util.Constants.POSTS
import com.aitechnologies.utripod.util.Constants.POSTS_HASH_TAGS
import com.aitechnologies.utripod.util.Constants.POSTS_TAGS
import com.aitechnologies.utripod.util.Constants.POST_COMMENTS
import com.aitechnologies.utripod.util.Constants.POST_COMMENTS_REPLY
import com.aitechnologies.utripod.util.Constants.POST_PROMOTIONS
import com.aitechnologies.utripod.util.Constants.POST_REPORTS
import com.aitechnologies.utripod.util.Constants.TRENDING_HASH_TAGS
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class PostRepository(private val application: Application) {

    private val firebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val notificationRepository by lazy { NotificationRepository(application) }

    suspend fun getPromotions(): List<PostPromotion> {
        return try {
            firebaseFirestore.collection(POST_PROMOTIONS)
                .whereGreaterThanOrEqualTo("toDate", Date(System.currentTimeMillis()))
                .limit(10)
                .get()
                .await()
                .toObjects(PostPromotion::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMyPromotions(): List<PostPromotion> {
        return try {
            firebaseFirestore.collection(POST_PROMOTIONS)
                .whereEqualTo("username", application.getUsername())
                .get()
                .await()
                .toObjects(PostPromotion::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMyFollowersPosts(blockedList: ArrayList<String>): List<Posts> {
        return try {
            val list: ArrayList<String> = arrayListOf()
            if (blockedList.isEmpty())
                blockedList.add(System.currentTimeMillis().toString())
            firebaseFirestore.collection(FOLLOWINGS)
                .document(application.getUsername())
                .collection(FOLLOWINGS)
                .whereNotIn("username", blockedList)
                .get()
                .await()
                .apply {
                    for (doc in this) {
                        list.add(doc.data["username"].toString())
                    }
                }

            list.add(application.getUsername())

            return firebaseFirestore.collection(POSTS)
                .whereIn("username", list)
                .limit(20)
                .get()
                .await()
                .toObjects(Posts::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTrendingPosts(): List<Posts> {
        return try {
            firebaseFirestore.collection(POSTS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(40)
                .get()
                .await()
                .toObjects(Posts::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPostById(id: String): List<Posts> {
        return try {
            firebaseFirestore.collection(POSTS)
                .whereEqualTo("id", id)
                .limit(1)
                .get()
                .await()
                .toObjects(Posts::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @DelicateCoroutinesApi
    suspend fun addPost(posts: Posts): DocumentReference? {
        val hashTags: List<String> =
            posts.hashTags.toString().replace("[", "").replace("]", "").split(",")
        hashTags.forEach {
            if (it.isNotEmpty() && it.isNotBlank()) {

                firebaseFirestore.collection(POSTS_HASH_TAGS).document(it)
                    .collection(POSTS_HASH_TAGS).add(
                        mapOf("tag" to it, "id" to posts.id)
                    )

                firebaseFirestore.collection(TRENDING_HASH_TAGS)
                    .whereEqualTo("tag", it)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { qs ->
                        if (qs.isEmpty) {
                            GlobalScope.launch(Dispatchers.IO) {
                                firebaseFirestore.collection(TRENDING_HASH_TAGS)
                                    .add(mapOf("tag" to it, "count" to 1))
                                    .await()
                            }

                        } else {
                            var count = 0
                            for (doc in qs) {
                                count = doc.data["count"].toString().toInt()
                            }
                            count += 1
                            GlobalScope.launch(Dispatchers.IO) {
                                firebaseFirestore.collection(TRENDING_HASH_TAGS)
                                    .document(qs.documents[0].id)
                                    .update(mapOf("count" to count))
                                    .await()
                            }
                        }
                    }
            }
        }

        val tagsList: List<String> =
            posts.tags.toString().replace("[", "").replace("]", "").split(",")

        tagsList.forEach {
            if (it.isNotEmpty() && it.isNotBlank()) {
                firebaseFirestore.collection(POSTS_TAGS).document(it).collection(POSTS_TAGS).add(
                    mapOf("id" to posts.id)
                )
            }
        }

        return firebaseFirestore.collection(POSTS)
            .add(posts)
            .await()

    }

    suspend fun updateTaggedPost(
        posts: Posts,
        deleteList: List<String>
    ) {
        deleteList.forEach {
            val delete = firebaseFirestore.collection(POSTS_TAGS)
                .document(it.trim()).collection(POSTS_TAGS)
                .whereEqualTo("id", posts.id)
                .limit(1)
                .get()
                .await()
            if (!delete.isEmpty) {
                firebaseFirestore.collection(POSTS_TAGS)
                    .document(it.trim()).collection(POSTS_TAGS)
                    .document(delete.documents[0].id)
                    .delete()
            }
        }
        val post = firebaseFirestore.collection(POSTS)
            .whereEqualTo("id", posts.id)
            .limit(1)
            .get()
            .await()
        if (!post.isEmpty) {
            firebaseFirestore.collection(POSTS)
                .document(post.documents[0].id)
                .update(mapOf("tags" to posts.tags))
        }
    }

    suspend fun likePost(posts: Posts) {
        val likes = firebaseFirestore.collection(POSTS)
            .whereEqualTo("id", posts.id)
            .limit(1)
            .get()
            .await()
        var likeList: List<String> = listOf()
        var likesCount = 0
        for (doc in likes.documents) {
            likesCount = doc.data?.get("likes")?.toString()!!.toInt()
            likeList =
                doc.data?.get("likesList")!!.toString().replace("]", "").replace("[", "").split(",")
        }

        val myLikedList: ArrayList<String> = arrayListOf()

        likeList.forEach {
            if (it.isNotBlank())
                myLikedList.add(it.trim())
        }

        var isLiked = false
        myLikedList.forEach {
            if (it.trim() == application.getUsername())
                isLiked = true
        }

        if (isLiked) {
            likesCount -= 1
            myLikedList.remove(application.getUsername())
        } else {
            if (posts.username != application.getUsername()) {
                val notification = Notification()
                notification.id = posts.id.toString()
                notification.message = application.getUsername() + " is liked your post"
                if (posts.type == 0)
                    notification.url = posts.profileUrl.toString()
                else
                    notification.url = posts.post.toString()
                notificationRepository.sendNotification(notification, posts.username.toString())
            }
            likesCount += 1
            myLikedList.add(application.getUsername())
        }

        if (likesCount < 0)
            likesCount = 0

        firebaseFirestore.collection(POSTS)
            .document(likes.documents[0].id)
            .update(
                mapOf(
                    "likesList" to myLikedList.toString(),
                    "likes" to likesCount
                )
            )
            .await()
    }

    suspend fun sharePost(posts: Posts): String {

        val link = "https://utripod.page.link/posts/${posts.id}"

        val qs = firebaseFirestore.collection(POSTS)
            .whereEqualTo("id", posts.id)
            .limit(1)
            .get()
            .await()

        var shares = 0

        if (qs.isEmpty)
            return link

        for (doc in qs.documents) {
            shares = doc!!.data?.get("shares")!!.toString().toInt()
        }

        shares += 1

        firebaseFirestore.collection(POSTS)
            .document(qs.documents[0].id)
            .update(mapOf("shares" to shares))
            .await()

        return link
    }

    suspend fun promotePost(promotion: PostPromotion): DocumentReference? {
        return firebaseFirestore.collection(POST_PROMOTIONS)
            .add(promotion)
            .await()
    }

    @ExperimentalCoroutinesApi
    suspend fun getComments(postId: String): Flow<Event<Resource<List<PostComment>>>> {
        return channelFlow {
            trySend(Event(Resource("loading")))
            val comments = firebaseFirestore.collection(POST_COMMENTS)
                .document(postId)
                .collection(POST_COMMENTS)
                .limit(60)
                .addSnapshotListener { value, error ->
                    if (error != null)
                        trySend(Event(Resource("error")))
                    else {
                        trySend(
                            Event(
                                Resource(
                                    "success",
                                    value!!.toObjects(PostComment::class.java)
                                )
                            )
                        )
                    }
                }
            awaitClose {
                comments.remove()
            }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getReplyComments(commentId: String): Flow<Event<Resource<List<PostComment>>>> {

        return channelFlow {
            trySend(Event(Resource("loading")))
            val comments = firebaseFirestore.collection(POST_COMMENTS_REPLY)
                .document(commentId)
                .collection(POST_COMMENTS_REPLY)
                .limit(60)
                .addSnapshotListener { value, error ->
                    if (error != null)
                        trySend(Event(Resource("error")))
                    else {
                        trySend(
                            Event(
                                Resource(
                                    "success",
                                    value!!.toObjects(PostComment::class.java)
                                )
                            )
                        )
                    }
                }
            awaitClose {
                comments.remove()
            }
        }
    }

    suspend fun addComment(postComment: PostComment): DocumentReference? {
        val post = firebaseFirestore.collection(POSTS)
            .whereEqualTo("id", postComment.postId)
            .limit(1)
            .get()
            .await()

        var comments = 0

        var profileUrl = ""
        for (doc in post.documents) {
            comments = doc.data!!["comments"].toString().toInt()
            profileUrl = doc.data!!["profileUrl"].toString()
        }

        comments += 1

        firebaseFirestore.collection(POSTS)
            .document(post.documents[0].id)
            .update(mapOf("comments" to comments))
            .await()

        val notification = Notification()
        notification.id = postComment.postId
        notification.message = application.getUsername() + " is commented on  your post"

        notification.url = profileUrl
        notification.viewType = 2
        notificationRepository.sendNotification(notification, postComment.username)

        return firebaseFirestore.collection(POST_COMMENTS)
            .document(postComment.postId)
            .collection(POST_COMMENTS)
            .add(postComment)
            .await()
    }

    suspend fun addReplyComment(
        postComment: PostComment,
        postId: String,
        isFirst: Boolean
    ): DocumentReference? {
        val post = if (isFirst) {
            firebaseFirestore.collection(POST_COMMENTS)
                .document(postId)
                .collection(POST_COMMENTS)
                .whereEqualTo("id", postComment.postId)
                .limit(1)
                .get()
                .await()
        } else {
            firebaseFirestore.collection(POST_COMMENTS_REPLY)
                .document(postId)
                .collection(POST_COMMENTS_REPLY)
                .whereEqualTo("id", postComment.postId)
                .limit(1)
                .get()
                .await()
        }


        var replies = 0

        for (doc in post.documents) {
            replies = doc.data!!["replies"].toString().toInt()
        }

        replies += 1

        if (isFirst) {
            firebaseFirestore.collection(POST_COMMENTS)
                .document(postId)
                .collection(POST_COMMENTS)
                .document(post.documents[0].id)
                .update(mapOf("replies" to replies))
                .await()
        } else {
            firebaseFirestore.collection(POST_COMMENTS_REPLY)
                .document(postId)
                .collection(POST_COMMENTS_REPLY)
                .document(post.documents[0].id)
                .update(mapOf("replies" to replies))
                .await()
        }


        return firebaseFirestore.collection(POST_COMMENTS_REPLY)
            .document(postComment.postId)
            .collection(POST_COMMENTS_REPLY)
            .add(postComment)
            .await()
    }

    suspend fun deleteReplyComment(
        id: String,
        postId: String,
        previousCommentId: String,
        isFirst: Boolean,
    ): Void? {
        var replies = 0
        if (isFirst) {
            val comment = firebaseFirestore.collection(POST_COMMENTS)
                .document(previousCommentId)
                .collection(POST_COMMENTS)
                .whereEqualTo("id", postId)
                .limit(1)
                .get()
                .await()

            for (doc in comment.documents) {
                replies = doc.data!!["replies"].toString().toInt()
            }
            replies -= 1
            if (comment.isEmpty)
                return null
            firebaseFirestore.collection(POST_COMMENTS)
                .document(previousCommentId)
                .collection(POST_COMMENTS)
                .document(comment.documents[0].id)
                .update(mapOf("replies" to replies))
                .await()
        } else {
            val comment = firebaseFirestore.collection(POST_COMMENTS_REPLY)
                .document(previousCommentId)
                .collection(POST_COMMENTS_REPLY)
                .whereEqualTo("id", postId)
                .limit(1)
                .get()
                .await()

            for (doc in comment.documents) {
                replies = doc.data!!["replies"].toString().toInt()
            }
            replies -= 1
            if (comment.isEmpty)
                return null
            firebaseFirestore.collection(POST_COMMENTS_REPLY)
                .document(previousCommentId)
                .collection(POST_COMMENTS_REPLY)
                .document(comment.documents[0].id)
                .update(mapOf("replies" to replies))
                .await()
        }

        val comment = firebaseFirestore.collection(POST_COMMENTS_REPLY)
            .document(postId)
            .collection(POST_COMMENTS_REPLY)
            .whereEqualTo("id", id)
            .limit(1)
            .get()
            .await()

        if (comment.isEmpty)
            return null

        return firebaseFirestore.collection(POST_COMMENTS_REPLY)
            .document(postId)
            .collection(POST_COMMENTS_REPLY)
            .document(comment.documents[0].id)
            .delete()
            .await()
    }

    suspend fun deleteComment(
        id: String,
        postId: String
    ): Void? {
        val comment = firebaseFirestore.collection(POST_COMMENTS)
            .document(postId)
            .collection(POST_COMMENTS)
            .whereEqualTo("id", id)
            .limit(1)
            .get()
            .await()

        if (comment.documents.isEmpty())
            return null

        val post = firebaseFirestore.collection(POSTS)
            .whereEqualTo("id", postId)
            .limit(1)
            .get()
            .await()

        if (post.documents.isNotEmpty()) {
            var comments = 0
            for (doc in post.documents) {
                comments = doc.data!!["comments"].toString().toInt()
            }

            comments -= 1
            if (comments < 0)
                comments = 0
            firebaseFirestore.collection(POSTS)
                .document(post.documents[0].id)
                .update(mapOf("comments" to comments))
                .await()
        }


        return firebaseFirestore.collection(POST_COMMENTS)
            .document(postId)
            .collection(POST_COMMENTS)
            .document(comment.documents[0].id)
            .delete()
            .await()

    }

    suspend fun updatePost(posts: Posts): Void? {
        val post = firebaseFirestore.collection(POSTS)
            .whereEqualTo("id", posts.id)
            .limit(1)
            .get()
            .await()

        if (post.documents.isEmpty())
            return null

        return firebaseFirestore.collection(POSTS)
            .document(post.documents[0].id)
            .update(
                mapOf(
                    "post" to posts.post,
                    "hashTags" to posts.hashTags!!
                        .replace(" ", "")
                        .split("#")
                        .toString()
                )
            ).await()

    }

    suspend fun reportPost(posts: Posts) {
        val report = firebaseFirestore.collection(POST_REPORTS)
            .whereEqualTo("id", posts.id)
            .limit(1)
            .get()
            .await()

        if (report.isEmpty) {
            firebaseFirestore.collection(POST_REPORTS)
                .add(
                    mapOf(
                        "id" to posts.id,
                        "reports" to 0
                    )
                )
        } else {
            var reports = 0
            for (doc in report.documents) {
                reports = doc.data!!["reports"].toString().toInt()
            }
            reports += 1
            firebaseFirestore.collection(POST_REPORTS)
                .document(report.documents[0].id)
                .update(mapOf("reports" to reports))
        }

    }

    suspend fun deletePost(posts: Posts): Void? {
        val post = firebaseFirestore.collection(POSTS)
            .whereEqualTo("id", posts.id)
            .limit(1)
            .get()
            .await()

        if (post.isEmpty || post.documents.isEmpty())
            return null

        return firebaseFirestore.collection(POSTS)
            .document(post.documents[0].id)
            .delete()
            .await()

    }

    suspend fun deletePromotion(posts: Posts): Void? {
        val post = firebaseFirestore.collection(POST_PROMOTIONS)
            .whereEqualTo("id", posts.id)
            .limit(1)
            .get()
            .await()

        if (post.isEmpty || post.documents.isEmpty())
            return null

        return firebaseFirestore.collection(POST_PROMOTIONS)
            .document(post.documents[0].id)
            .delete()
            .await()

    }

    @ExperimentalCoroutinesApi
    suspend fun getMyPosts(username: String): Flow<Event<Resource<List<Posts>>>> {
        return channelFlow {
            val posts = firebaseFirestore.collection(POSTS)
                .whereEqualTo("username", username)
                .addSnapshotListener { value, error ->
                    if (error != null)
                        trySend(Event(Resource("error")))
                    else {
                        trySend(Event(Resource("success", value!!.toObjects(Posts::class.java))))
                    }

                }
            awaitClose { posts.remove() }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getTaggedPosts(username: String): Flow<Event<Resource<List<Posts>>>> {
        return channelFlow {
            val posts = firebaseFirestore.collection(POSTS_TAGS)
                .document(username)
                .collection(POSTS_TAGS)
                .addSnapshotListener { value, error ->
                    if (error != null)
                        trySend(Event(Resource("error")))
                    else {
                        val idList: ArrayList<String> = arrayListOf()
                        for (doc in value!!.documents) {
                            idList.add(doc.data!!["id"].toString())
                        }
                        if (idList.isEmpty()) {
                            trySend(Event(Resource("empty")))
                        } else {

                            firebaseFirestore.collection(POSTS)
                                .whereIn("id", idList)
                                .get()
                                .addOnSuccessListener {
                                    trySend(
                                        Event(
                                            Resource(
                                                "success",
                                                it.toObjects(Posts::class.java)
                                            )
                                        )
                                    )
                                }
                                .addOnFailureListener {
                                    trySend(Event(Resource("error")))
                                }
                        }
                    }

                }
            awaitClose { posts.remove() }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getPostsByHashtags(hashtag: String): Flow<List<Posts>> {
        return channelFlow {
            firebaseFirestore.collection(POSTS_HASH_TAGS)
                .document(hashtag.trim())
                .collection(POSTS_HASH_TAGS)
                .limit(40)
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        val list: ArrayList<String> = arrayListOf()
                        for (doc in it.documents) {
                            list.add(doc.data!!["id"].toString())
                        }
                        firebaseFirestore.collection(POSTS)
                            .whereIn("id", list)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                trySend(snapshot.toObjects(Posts::class.java))
                            }

                    }
                }

            awaitClose { }
        }
    }


    suspend fun getTrendingHashTags(): MutableList<HashTag> {
        return try {
            firebaseFirestore.collection(TRENDING_HASH_TAGS)
                .orderBy("count", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
                .toObjects(HashTag::class.java)

        } catch (e: Exception) {
            arrayListOf()
        }
    }

}