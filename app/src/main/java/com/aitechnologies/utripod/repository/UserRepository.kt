package com.aitechnologies.utripod.repository

import android.app.Application
import com.aitechnologies.utripod.models.Notification
import com.aitechnologies.utripod.models.SocialLinks
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getProfileUrl
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.Constants.BLOCKED_USERS
import com.aitechnologies.utripod.util.Constants.CONNECTION
import com.aitechnologies.utripod.util.Constants.FOLLOWERS
import com.aitechnologies.utripod.util.Constants.FOLLOWINGS
import com.aitechnologies.utripod.util.Constants.SOCIAL_LINKS
import com.aitechnologies.utripod.util.Constants.USERS
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val firebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    suspend fun searchUser(user: String): List<Users> {
        return try {
            firebaseFirestore.collection(USERS)
                .orderBy("username")
                .startAt(user.uppercase())
                .endAt(user + "\uf8ff")
                .limit(10)
                .get()
                .await()
                .toObjects(Users::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserProfile(user: String): List<Users> {

        val list = firebaseFirestore.collection(USERS)
            .whereEqualTo("username", user)
            .limit(1)
            .get()
            .await()
            .toObjects(Users::class.java)

        return if (list.isEmpty())
            emptyList()
        else
            list
    }

    suspend fun getSocialLinks(user: String): List<SocialLinks> {
        return firebaseFirestore.collection(SOCIAL_LINKS)
            .document(user)
            .collection(SOCIAL_LINKS)
            .get()
            .await()
            .toObjects(SocialLinks::class.java)
    }

    suspend fun addSocialLinks(
        socialLinks: SocialLinks,
        username: String
    ) {
        val links = firebaseFirestore.collection(SOCIAL_LINKS)
            .document(username)
            .collection(SOCIAL_LINKS)
            .limit(1)
            .get()
            .await()
        if (links.isEmpty) {
            firebaseFirestore.collection(SOCIAL_LINKS)
                .document(username)
                .collection(SOCIAL_LINKS)
                .add(socialLinks)
                .await()
        } else {
            firebaseFirestore.collection(SOCIAL_LINKS)
                .document(username)
                .collection(SOCIAL_LINKS)
                .document(links.documents[0].id)
                .update(
                    mapOf(
                        "linkedin" to socialLinks.linkedin,
                        "fb" to socialLinks.fb,
                        "youtube" to socialLinks.youtube,
                        "twitter" to socialLinks.twitter,
                        "insta" to socialLinks.insta,
                    )
                ).await()
        }

    }

    suspend fun updateProfile(users: Users): Boolean {
        val user = firebaseFirestore.collection(USERS)
            .whereEqualTo("username", users.username)
            .limit(1)
            .get()
            .await()

        return try {
            firebaseFirestore.collection(USERS)
                .document(user.documents[0].id)
                .update(
                    mapOf(
                        "name" to users.name,
                        "profileUrl" to users.profileUrl,
                        "profession" to users.profession,
                        "bio" to users.bio
                    )
                ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updatePassword(users: String, password: String): Boolean {
        val user = firebaseFirestore.collection(USERS)
            .whereEqualTo("username", users)
            .limit(1)
            .get()
            .await()

        return try {
            firebaseFirestore.collection(USERS)
                .document(user.documents[0].id)
                .update(
                    mapOf(
                        "password" to password,
                    )
                ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUsersByRegion(region: String): List<Users> {
        return try {
            firebaseFirestore.collection(USERS)
                .whereEqualTo("region", region)
                .get()
                .await()
                .toObjects(Users::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUsersByLocation(location: String): List<Users> {
        return try {
            firebaseFirestore.collection(USERS)
                .whereEqualTo("location", location)
                .get()
                .await()
                .toObjects(Users::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllUsers(): List<Users> {
        return try {
            firebaseFirestore.collection(USERS)
                .get()
                .await()
                .toObjects(Users::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getBlockedUsers(username: String): List<String> {
        return try {
            val blocked = firebaseFirestore.collection(BLOCKED_USERS)
                .document(username)
                .collection(BLOCKED_USERS)
                .get()
                .await()
            val list: ArrayList<String> = arrayListOf()
            return if (blocked.isEmpty)
                emptyList()
            else {
                for (i in blocked.documents) {
                    list.add(i.data!!["username"].toString())
                }
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun blockUser(
        username: String,
        blockedUser: String
    ) {
        firebaseFirestore.collection(BLOCKED_USERS)
            .document(username)
            .collection(BLOCKED_USERS)
            .add(mapOf("username" to blockedUser))
            .await()
    }

    suspend fun unblockUser(
        username: String,
        blockedUser: String
    ) {
        val blocked = firebaseFirestore.collection(BLOCKED_USERS)
            .document(username)
            .collection(BLOCKED_USERS)
            .whereEqualTo("username", blockedUser)
            .limit(1)
            .get()
            .await()

        if (!blocked.isEmpty) {
            firebaseFirestore.collection(BLOCKED_USERS)
                .document(username)
                .collection(BLOCKED_USERS)
                .document(blocked.documents[0].id)
                .delete()
                .await()
        }

    }


    suspend fun getUserByPhone(phone: String): List<Users> {
        if (firebaseAuth.currentUser == null)
            firebaseAuth.signInAnonymously().await()
        return try {
            firebaseFirestore.collection(USERS)
                .whereEqualTo("phone", phone)
                .limit(1)
                .get()
                .await()
                .toObjects(Users::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun resetPassword(
        phone: String,
        password: String
    ): Void? {
        val user = firebaseFirestore.collection(USERS)
            .whereEqualTo("phone", phone)
            .limit(1)
            .get()
            .await()

        return firebaseFirestore.collection(USERS)
            .document(user.documents[0].id)
            .update(mapOf("password" to password))
            .await()

    }

    suspend fun getUsersByUsernameList(
        usernameList: List<String>
    ): List<Users> {
        return try {
            firebaseFirestore.collection(USERS)
                .whereIn("username", usernameList)
                .get()
                .await()
                .toObjects(Users::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserByGender(gender: String): List<Users> {
        return try {
            firebaseFirestore.collection(USERS)
                .whereEqualTo("gender", gender)
                .limit(10)
                .get()
                .await()
                .toObjects(Users::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserByProfession(profession: String): List<Users> {
        return try {
            firebaseFirestore.collection(USERS)
                .whereEqualTo("profession", profession)
                .limit(10)
                .get()
                .await()
                .toObjects(Users::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserByAge(age: String): List<Users> {
        return try {
            firebaseFirestore.collection(USERS)
                .whereEqualTo("age", age)
                .limit(10)
                .get()
                .await()
                .toObjects(Users::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun checkIsFollowing(
        application: Application,
        username: String
    ): Flow<Event<Resource<Boolean>>> {
        return channelFlow {
            val listener = firebaseFirestore.collection(FOLLOWINGS)
                .document(application.getUsername())
                .collection(FOLLOWINGS)
                .whereEqualTo("username", username)
                .limit(1)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(Event(Resource("error")))
                    } else {
                        if (value!!.isEmpty) {
                            trySend(Event(Resource("success", false)))
                        } else {
                            trySend(Event(Resource("success", true)))
                        }
                    }
                }
            awaitClose { listener.remove() }
        }
    }

    @DelicateCoroutinesApi
    suspend fun followOrUnfollow(
        username: String,
        application: Application
    ) {
        val isFollowing = firebaseFirestore.collection(FOLLOWINGS)
            .document(application.getUsername())
            .collection(FOLLOWINGS)
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .await()

        if (isFollowing.isEmpty) {
            firebaseFirestore.collection(FOLLOWINGS)
                .document(application.getUsername())
                .collection(FOLLOWINGS)
                .add(mapOf("username" to username))
                .addOnSuccessListener {
                    firebaseFirestore.collection(USERS)
                        .whereEqualTo("username", application.getUsername())
                        .limit(1)
                        .get()
                        .addOnSuccessListener { qs ->
                            var following = qs.toObjects(Users::class.java)[0].following
                            following += 1
                            firebaseFirestore.collection(USERS)
                                .document(qs.documents[0].id)
                                .update(mapOf("following" to following))
                        }


                    firebaseFirestore.collection(USERS)
                        .whereEqualTo("username", username)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { qs ->
                            var followers = qs.toObjects(Users::class.java)[0].followers
                            followers += 1
                            firebaseFirestore.collection(USERS)
                                .document(qs.documents[0].id)
                                .update(mapOf("followers" to followers))
                        }

                    firebaseFirestore.collection(FOLLOWERS)
                        .document(username)
                        .collection(FOLLOWERS)
                        .add(mapOf("username" to application.getUsername()))

                    val notification = Notification()
                    notification.id = application.getUsername()
                    notification.message =
                        application.getUsername() + " is started to following you"
                    notification.url = application.getProfileUrl()
                    notification.viewType = 2

                    GlobalScope.launch {
                        NotificationRepository(application).sendNotification(notification, username)
                    }

                }
        } else {
            firebaseFirestore.collection(FOLLOWINGS)
                .document(application.getUsername())
                .collection(FOLLOWINGS)
                .document(isFollowing.documents[0].id)
                .delete()
                .addOnSuccessListener {
                    firebaseFirestore.collection(USERS)
                        .whereEqualTo("username", application.getUsername())
                        .limit(1)
                        .get()
                        .addOnSuccessListener { qs ->
                            var following = qs.toObjects(Users::class.java)[0].following
                            following -= 1
                            firebaseFirestore.collection(USERS)
                                .document(qs.documents[0].id)
                                .update(mapOf("following" to following))
                        }
                    firebaseFirestore.collection(USERS)
                        .whereEqualTo("username", username)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { qs ->
                            var followers = qs.toObjects(Users::class.java)[0].followers
                            followers -= 1
                            firebaseFirestore.collection(USERS)
                                .document(qs.documents[0].id)
                                .update(mapOf("followers" to followers))
                        }

                    firebaseFirestore.collection(FOLLOWERS)
                        .document(username)
                        .collection(FOLLOWERS)
                        .whereEqualTo("username",application.getUsername())
                        .limit(1)
                        .get()
                        .addOnSuccessListener {
                            firebaseFirestore.collection(FOLLOWERS)
                                .document(username)
                                .collection(FOLLOWERS)
                                .document(it.documents[0].id)
                                .delete()
                        }

                }
        }
    }

    suspend fun setBioAndImage(
        username: String,
        profileUrl: String,
        bio: String
    ) {
        val user = firebaseFirestore.collection(USERS)
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .await()

        firebaseFirestore.collection(USERS)
            .document(user.documents[0].id)
            .update(mapOf("bio" to bio, "profileUrl" to profileUrl))
            .await()

    }

    suspend fun getMyFollowings(
        username: String,
    ): ArrayList<String> {
        val followings = firebaseFirestore.collection(FOLLOWINGS)
            .document(username)
            .collection(FOLLOWINGS)
            .get()
            .await()

        val followingList:ArrayList<String> = arrayListOf()

        return if (followings.isEmpty) arrayListOf() else{
            for (doc in followings.documents){
                followingList.add(doc.data!!["username"].toString())
            }
            followingList
        }

    }

    suspend fun getMyFollowers(
        username: String,
    ): ArrayList<String> {
        val followings = firebaseFirestore.collection(FOLLOWERS)
            .document(username)
            .collection(FOLLOWERS)
            .get()
            .await()

        val followingList:ArrayList<String> = arrayListOf()

        return if (followings.isEmpty) arrayListOf() else{
            for (doc in followings.documents){
                followingList.add(doc.data!!["username"].toString())
            }
            followingList
        }

    }


    @ExperimentalCoroutinesApi
    suspend fun getOnlineUsers(usernameList: ArrayList<String>):Flow<ArrayList<String>>{
        return callbackFlow {
            firebaseFirestore.collection(CONNECTION)
                .whereIn("username",usernameList)
                .whereEqualTo("isOnline",true)
                .get()
                .addOnSuccessListener {
                    val list:ArrayList<String> = arrayListOf()
                    for (doc in it){
                        list.add(doc.data["username"].toString())
                    }
                    trySend(list)
                }
            awaitClose {  }
        }
    }

}