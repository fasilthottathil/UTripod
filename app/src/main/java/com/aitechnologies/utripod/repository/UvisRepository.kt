package com.aitechnologies.utripod.repository

import android.app.Application
import com.aitechnologies.utripod.models.Notification
import com.aitechnologies.utripod.models.PostComment
import com.aitechnologies.utripod.models.Uvis
import com.aitechnologies.utripod.models.UvisPromotion
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.Constants
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class UvisRepository(private val application: Application) {
    private val firebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val notificationRepository by lazy { NotificationRepository(application) }

    suspend fun getPromotions(): List<UvisPromotion> {
        return try {
            firebaseFirestore.collection(Constants.UVIS_PROMOTIONS)
                .limit(10)
                .whereGreaterThanOrEqualTo("toDate", Date(System.currentTimeMillis()))
                .get()
                .await()
                .toObjects(UvisPromotion::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMyPromotions(): List<UvisPromotion> {
        return try {
            firebaseFirestore.collection(Constants.UVIS_PROMOTIONS)
                .whereEqualTo("username", application.getUsername())
                .get()
                .await()
                .toObjects(UvisPromotion::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMyFollowersUvis(): List<Uvis> {
        return try {
            val list: ArrayList<String> = arrayListOf()
            firebaseFirestore.collection(Constants.FOLLOWINGS)
                .document(application.getUsername())
                .collection(Constants.FOLLOWINGS)
                .get()
                .await()
                .apply {
                    for (doc in this) {
                        list.add(doc.data["username"].toString())
                    }
                }

            list.add(application.getUsername())

            return firebaseFirestore.collection(Constants.UVIS)
                .whereIn("username", list)
                .limit(20)
                .get()
                .await()
                .toObjects(Uvis::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMyUvis(username: String): List<Uvis> {
        return try {
            return firebaseFirestore.collection(Constants.UVIS)
                .whereEqualTo("username",username)
                .get()
                .await()
                .toObjects(Uvis::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTrendingUvis(): List<Uvis> {
        return try {
            firebaseFirestore.collection(Constants.UVIS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(40)
                .get()
                .await()
                .toObjects(Uvis::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun addUvis(uvis: Uvis): DocumentReference? {
        val hashTags: List<String> =
            uvis.hashTags.toString().replace("[", "").replace("]", "").split(",")
        hashTags.forEach {
            if (it.isNotEmpty() && it.isNotBlank())
                firebaseFirestore.collection(Constants.UVIS_HASH_TAGS).document(it)
                    .collection(Constants.UVIS_HASH_TAGS).add(
                        mapOf("tag" to it, "id" to uvis.id)
                    )
        }

        val tagsList: List<String> =
            uvis.tags.toString().replace("[", "").replace("]", "").split(",")

        tagsList.forEach {
            if (it.isNotEmpty() && it.isNotBlank())
                firebaseFirestore.collection(Constants.UVIS_TAGS).document(it)
                    .collection(Constants.UVIS_TAGS).add(
                    mapOf("id" to uvis.id)
                )
        }

        return firebaseFirestore.collection(Constants.UVIS)
            .add(uvis)
            .await()

    }

    suspend fun likeUvis(uvis: Uvis) {
        val likes = firebaseFirestore.collection(Constants.UVIS)
            .whereEqualTo("id", uvis.id)
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
            if (it.isNotBlank()) {
                myLikedList.add(it.trim())
            }
        }

        if (myLikedList.contains(application.getUsername())) {
            likesCount -= 1
            myLikedList.remove(application.getUsername())
        } else {
            val notification = Notification()
            notification.id = uvis.id.toString()
            notification.message = application.getUsername() + " is liked your uvis"

            notification.url = uvis.url.toString()
            notification.viewType = 1
            notificationRepository.sendNotification(notification, uvis.username.toString())
            likesCount += 1
            myLikedList.add(application.getUsername())
        }

        if (likesCount < 0)
            likesCount = 0

        firebaseFirestore.collection(Constants.UVIS)
            .document(likes.documents[0].id)
            .update(
                mapOf(
                    "likesList" to myLikedList.toString(),
                    "likes" to likesCount
                )
            )
            .await()
    }

    suspend fun shareUvis(uvis: Uvis): String {

        val link = "https://utripod.page.link/uvis/${uvis.id}"

        val qs = firebaseFirestore.collection(Constants.UVIS)
            .whereEqualTo("id", uvis.id)
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

        firebaseFirestore.collection(Constants.UVIS)
            .document(qs.documents[0].id)
            .update(mapOf("shares" to shares))
            .await()

        return link
    }

    suspend fun promoteUvis(promotion: UvisPromotion): DocumentReference? {
        return firebaseFirestore.collection(Constants.UVIS_PROMOTIONS)
            .add(promotion)
            .await()
    }

    @ExperimentalCoroutinesApi
    suspend fun getComments(postId: String): Flow<Event<Resource<List<PostComment>>>> {
        return channelFlow {
            trySend(Event(Resource("loading")))
            val comments = firebaseFirestore.collection(Constants.UVIS_COMMENTS)
                .document(postId)
                .collection(Constants.UVIS_COMMENTS)
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
            val comments = firebaseFirestore.collection(Constants.UVIS_COMMENT_REPLY)
                .document(commentId)
                .collection(Constants.UVIS_COMMENT_REPLY)
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
        val post = firebaseFirestore.collection(Constants.UVIS)
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

        firebaseFirestore.collection(Constants.UVIS)
            .document(post.documents[0].id)
            .update(mapOf("comments" to comments))
            .await()


        val notification = Notification()
        notification.id = postComment.postId
        notification.message = application.getUsername() + " is commented on  your uvis"

        notification.url = profileUrl
        notification.viewType = 1
        notificationRepository.sendNotification(notification, postComment.username)

        return firebaseFirestore.collection(Constants.UVIS_COMMENTS)
            .document(postComment.postId)
            .collection(Constants.UVIS_COMMENTS)
            .add(postComment)
            .await()


    }

    suspend fun addReplyComment(
        postComment: PostComment,
        postId: String,
        isFirst: Boolean
    ): DocumentReference? {
        val post = if (isFirst) {
            firebaseFirestore.collection(Constants.UVIS_COMMENTS)
                .document(postId)
                .collection(Constants.UVIS_COMMENTS)
                .whereEqualTo("id", postComment.postId)
                .limit(1)
                .get()
                .await()
        } else {
            firebaseFirestore.collection(Constants.UVIS_COMMENT_REPLY)
                .document(postId)
                .collection(Constants.UVIS_COMMENT_REPLY)
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
            firebaseFirestore.collection(Constants.UVIS_COMMENTS)
                .document(postId)
                .collection(Constants.UVIS_COMMENTS)
                .document(post.documents[0].id)
                .update(mapOf("replies" to replies))
                .await()
        } else {
            firebaseFirestore.collection(Constants.UVIS_COMMENT_REPLY)
                .document(postId)
                .collection(Constants.UVIS_COMMENT_REPLY)
                .document(post.documents[0].id)
                .update(mapOf("replies" to replies))
                .await()
        }


        return firebaseFirestore.collection(Constants.UVIS_COMMENT_REPLY)
            .document(postComment.postId)
            .collection(Constants.UVIS_COMMENT_REPLY)
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
            val comment = firebaseFirestore.collection(Constants.UVIS_COMMENTS)
                .document(previousCommentId)
                .collection(Constants.UVIS_COMMENTS)
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
            firebaseFirestore.collection(Constants.UVIS_COMMENTS)
                .document(previousCommentId)
                .collection(Constants.UVIS_COMMENTS)
                .document(comment.documents[0].id)
                .update(mapOf("replies" to replies))
                .await()
        } else {
            val comment = firebaseFirestore.collection(Constants.UVIS_COMMENT_REPLY)
                .document(previousCommentId)
                .collection(Constants.UVIS_COMMENT_REPLY)
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
            firebaseFirestore.collection(Constants.UVIS_COMMENT_REPLY)
                .document(previousCommentId)
                .collection(Constants.UVIS_COMMENT_REPLY)
                .document(comment.documents[0].id)
                .update(mapOf("replies" to replies))
                .await()
        }

        val comment = firebaseFirestore.collection(Constants.UVIS_COMMENT_REPLY)
            .document(postId)
            .collection(Constants.UVIS_COMMENT_REPLY)
            .whereEqualTo("id", id)
            .limit(1)
            .get()
            .await()

        if (comment.isEmpty)
            return null

        return firebaseFirestore.collection(Constants.UVIS_COMMENT_REPLY)
            .document(postId)
            .collection(Constants.UVIS_COMMENT_REPLY)
            .document(comment.documents[0].id)
            .delete()
            .await()
    }

    suspend fun deleteComment(
        id: String,
        postId: String
    ): Void? {
        val comment = firebaseFirestore.collection(Constants.UVIS_COMMENTS)
            .document(postId)
            .collection(Constants.UVIS_COMMENTS)
            .whereEqualTo("id", id)
            .limit(1)
            .get()
            .await()

        if (comment.documents.isEmpty())
            return null

        val post = firebaseFirestore.collection(Constants.UVIS)
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
            firebaseFirestore.collection(Constants.UVIS)
                .document(post.documents[0].id)
                .update(mapOf("comments" to comments))
                .await()
        }


        return firebaseFirestore.collection(Constants.UVIS_COMMENTS)
            .document(postId)
            .collection(Constants.UVIS_COMMENTS)
            .document(comment.documents[0].id)
            .delete()
            .await()

    }

    suspend fun updateUvis(uvis: Uvis): Void? {
        val post = firebaseFirestore.collection(Constants.UVIS)
            .whereEqualTo("id", uvis.id)
            .limit(1)
            .get()
            .await()

        if (post.documents.isEmpty())
            return null

        return firebaseFirestore.collection(Constants.UVIS)
            .document(post.documents[0].id)
            .update(
                mapOf(
                    "url" to uvis.url,
                    "hashTags" to uvis.hashTags!!
                        .replace(" ", "")
                        .split("#")
                        .toString()
                )
            ).await()

    }

    suspend fun reportUvis(uvis: Uvis) {
        val report = firebaseFirestore.collection(Constants.UVIS_REPORTS)
            .whereEqualTo("id", uvis.id)
            .limit(1)
            .get()
            .await()

        if (report.isEmpty) {
            firebaseFirestore.collection(Constants.UVIS_REPORTS)
                .add(
                    mapOf(
                        "id" to uvis.id,
                        "reports" to 0
                    )
                )
        } else {
            var reports = 0
            for (doc in report.documents) {
                reports = doc.data!!["reports"].toString().toInt()
            }
            reports += 1
            firebaseFirestore.collection(Constants.UVIS_REPORTS)
                .document(report.documents[0].id)
                .update(mapOf("reports" to reports))
        }

    }

    suspend fun deleteUvis(uvis: Uvis): Void? {
        val post = firebaseFirestore.collection(Constants.UVIS)
            .whereEqualTo("id", uvis.id)
            .limit(1)
            .get()
            .await()

        if (post.isEmpty || post.documents.isEmpty())
            return null

        return firebaseFirestore.collection(Constants.UVIS)
            .document(post.documents[0].id)
            .delete()
            .await()

    }

    suspend fun deletePromotion(uvis: Uvis): Void? {
        val post = firebaseFirestore.collection(Constants.UVIS_PROMOTIONS)
            .whereEqualTo("id", uvis.id)
            .limit(1)
            .get()
            .await()

        if (post.isEmpty || post.documents.isEmpty())
            return null

        return firebaseFirestore.collection(Constants.UVIS_REPORTS)
            .document(post.documents[0].id)
            .delete()
            .await()

    }

//    @ExperimentalCoroutinesApi
//    suspend fun getMyUvis(username: String): Flow<Event<Resource<List<Uvis>>>> {
//        return channelFlow {
//            val uvis = firebaseFirestore.collection(Constants.UVIS)
//                .whereEqualTo("username", username)
//                .addSnapshotListener { value, error ->
//                    if (error != null)
//                        trySend(Event(Resource("error")))
//                    else {
//                        trySend(Event(Resource("success", value!!.toObjects(Uvis::class.java))))
//                    }
//
//                }
//            awaitClose { uvis.remove() }
//        }
//    }

    @ExperimentalCoroutinesApi
    suspend fun getUvisById(id: String): Flow<List<Uvis>> {
        return channelFlow {
            firebaseFirestore.collection(Constants.UVIS)
                .whereEqualTo("id", id)
                .limit(1)
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty)
                        trySend(it.toObjects(Uvis::class.java))
                    else trySend(emptyList())
                }
            awaitClose { }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getTaggedUvis(username: String): Flow<Event<Resource<List<Uvis>>>> {
        return channelFlow {
            val uvis = firebaseFirestore.collection(Constants.UVIS_TAGS)
                .document(username)
                .collection(Constants.UVIS_TAGS)
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

                            firebaseFirestore.collection(Constants.UVIS)
                                .whereIn("id", idList)
                                .get()
                                .addOnSuccessListener {
                                    trySend(
                                        Event(
                                            Resource(
                                                "success",
                                                it.toObjects(Uvis::class.java)
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
            awaitClose { uvis.remove() }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getUvisByHashtags(hashtag: String): Flow<List<Uvis>> {
        return channelFlow {
            val idList = firebaseFirestore.collection(Constants.UVIS_HASH_TAGS)
                .document(hashtag)
                .collection(Constants.UVIS_HASH_TAGS)
                .limit(40)
                .get()
                .await()
            if (!idList.isEmpty) {
                val list: ArrayList<String> = arrayListOf()
                for (doc in idList.documents) {
                    list.add(doc.data!!["id"].toString())
                }
                firebaseFirestore.collection(Constants.UVIS)
                    .whereIn("id", list)
                    .get()
                    .addOnSuccessListener {
                        trySend(it.toObjects(Uvis::class.java))
                    }

            }
            awaitClose { }
        }
    }

}