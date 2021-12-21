package com.aitechnologies.utripod.repository

import android.app.Application
import com.aitechnologies.utripod.models.*
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getProfileUrl
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.Constants.GROUPS
import com.aitechnologies.utripod.util.Constants.GROUP_CHATS
import com.aitechnologies.utripod.util.Constants.GROUP_MEMBERS
import com.aitechnologies.utripod.util.Constants.MY_CHATS
import com.aitechnologies.utripod.util.Constants.MY_GROUPS
import com.aitechnologies.utripod.util.Constants.PRIVATE_CHATS
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.tasks.await

class ChatsRepository {
    private val firebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    @ExperimentalCoroutinesApi
    suspend fun getMyChats(username: String): Flow<Event<Resource<List<Chats>>>> {
        return channelFlow {
            val chats = firebaseFirestore.collection(MY_CHATS)
                .document(username)
                .collection(MY_CHATS)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(Event(Resource("error")))
                    } else {
                        trySend(Event(Resource("success", value!!.toObjects(Chats::class.java))))
                    }
                }
            awaitClose {
                chats.remove()
            }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getMyChatsByIdList(
        username: String,
        list: ArrayList<String>
    ): Flow<Event<Resource<List<Chats>>>> {
        return channelFlow {
            val chats = firebaseFirestore.collection(MY_CHATS)
                .document(username)
                .collection(MY_CHATS)
                .whereIn("username",list)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(Event(Resource("error")))
                    } else {
                        trySend(Event(Resource("success", value!!.toObjects(Chats::class.java))))
                    }
                }
            awaitClose {
                chats.remove()
            }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getJoinedGroupsIdList(username: String): Flow<Event<Resource<List<String>>>> {
        return channelFlow {
            val groups = firebaseFirestore.collection(MY_GROUPS)
                .document(username)
                .collection(MY_GROUPS)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(Event(Resource("error")))
                    } else {
                        val list: ArrayList<String> = arrayListOf()
                        for (doc in value!!.documents) {
                            list.add(doc.data!!["roomId"].toString())
                        }
                        trySend(Event(Resource("success", list)))
                    }
                }

            awaitClose {
                groups.remove()
            }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getJoinedGroups(idList: List<String>): Flow<Event<Resource<List<Groups>>>> {
        return channelFlow {
            if (idList.isEmpty()) {
                trySend(Event(Resource("empty")))
            } else {
                val groups = firebaseFirestore.collection(GROUPS)
                    .whereIn("roomId", idList)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            trySend(Event(Resource("error")))
                        } else {
                            val list: ArrayList<String> = arrayListOf()
                            for (doc in value!!.documents) {
                                list.add(doc.data!!["roomId"].toString())
                            }
                            trySend(Event(Resource("success", value.toObjects(Groups::class.java))))
                        }
                    }
                awaitClose {
                    groups.remove()
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getGroupById(id: String): Flow<Event<Resource<List<Groups>>>> {
        return channelFlow {
            firebaseFirestore.collection(GROUPS)
                .whereEqualTo("roomId", id)
                .limit(1)
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        trySend(Event(Resource("error")))
                    } else {
                        trySend(Event(Resource("success", it.toObjects(Groups::class.java))))
                    }
                }
            awaitClose {}
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun createGroup(groups: Groups): Flow<Boolean> {
        firebaseFirestore.collection(GROUPS)
            .add(groups)
            .await()

        val admin = groups.admins.replace("[", "")
            .replace("]", "")


        return callbackFlow {
            firebaseFirestore.collection(MY_GROUPS)
                .document(admin)
                .collection(MY_GROUPS)
                .add(mapOf("roomId" to groups.roomId))
                .addOnSuccessListener {
                    trySend(true)
                }
                .addOnFailureListener {
                    trySend(false)
                }
            awaitClose {}
        }
    }

    @ExperimentalCoroutinesApi
    fun getGroupChatMessages(roomId: String): Flow<Event<Resource<List<GroupMessage>>>> {
        return channelFlow {
            val groupChats = firebaseFirestore.collection(GROUP_CHATS)
                .document(roomId)
                .collection(GROUP_CHATS)
                .limit(60)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(Event(Resource("error")))
                    } else {
                        trySend(
                            Event(
                                Resource(
                                    "success",
                                    value!!.toObjects(GroupMessage::class.java)
                                )
                            )
                        )
                    }
                }
            awaitClose { groupChats.remove() }
        }
    }

    suspend fun sendGroupMessage(
        roomId: String,
        groupMessage: GroupMessage
    ) {
        val message = when (groupMessage.type) {
            0 -> groupMessage.message
            1 -> "Image"
            else -> "Video"
        }

        firebaseFirestore.collection(GROUP_CHATS)
            .document(roomId)
            .collection(GROUP_CHATS)
            .add(groupMessage)
            .await()

        firebaseFirestore.collection(GROUPS)
            .whereEqualTo("roomId", roomId)
            .limit(1)
            .get()
            .addOnSuccessListener {
                firebaseFirestore.collection(GROUPS)
                    .document(it.documents[0].id)
                    .update(mapOf("message" to message, "timestamp" to Timestamp.now()))
            }
    }

    @ExperimentalCoroutinesApi
    fun getGroupMembers(roomId: String): Flow<Event<Resource<ArrayList<String>>>> {
        return channelFlow {
            firebaseFirestore.collection(GROUP_CHATS)
                .document(roomId)
                .collection(GROUP_MEMBERS)
                .get()
                .addOnSuccessListener {
                    val list: ArrayList<String> = arrayListOf()
                    if (!it.isEmpty) {
                        for (doc in it) {
                            list.add(doc.data["username"].toString())
                        }
                    }
                    trySend(Event(Resource("success", list)))
                }
                .addOnFailureListener {
                    trySend(Event(Resource("error")))
                }
            awaitClose {}
        }
    }

    @ExperimentalCoroutinesApi
    fun addGroupMember(
        roomId: String,
        username: String
    ): Flow<Event<Resource<String>>> {
        return channelFlow {
            firebaseFirestore.collection(GROUP_CHATS)
                .document(roomId)
                .collection(GROUP_MEMBERS)
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        firebaseFirestore.collection(GROUP_CHATS)
                            .document(roomId)
                            .collection(GROUP_MEMBERS)
                            .add(mapOf("username" to username))
                            .addOnSuccessListener {
                                firebaseFirestore.collection(GROUPS)
                                    .whereEqualTo("roomId", roomId)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener { qs ->
                                        var members = 0
                                        for (doc in qs) {
                                            members = doc.data["members"].toString().toInt()
                                        }
                                        members += 1
                                        firebaseFirestore.collection(GROUPS)
                                            .document(qs.documents[0].id)
                                            .update(mapOf("members" to members))
                                            .addOnSuccessListener {
                                                trySend(Event(Resource("success", "success")))
                                            }
                                            .addOnFailureListener { exception ->
                                                trySend(
                                                    Event(
                                                        Resource(
                                                            "error",
                                                            exception.message.toString()
                                                        )
                                                    )
                                                )
                                            }
                                    }
                                    .addOnFailureListener { exception ->
                                        trySend(
                                            Event(
                                                Resource(
                                                    "error",
                                                    exception.message.toString()
                                                )
                                            )
                                        )
                                    }
                            }
                            .addOnFailureListener { exception ->
                                trySend(Event(Resource("error", exception.message.toString())))
                            }
                    } else {
                        trySend(Event(Resource("exist", "User already exist on your group")))
                    }
                }
                .addOnFailureListener {
                    trySend(Event(Resource("error", it.message.toString())))
                }
            awaitClose { }
        }
    }

    suspend fun editGroup(
        roomId: String,
        groups: Groups
    ): Void? {
        val group = firebaseFirestore.collection(GROUPS)
            .whereEqualTo("roomId", roomId)
            .limit(1)
            .get()
            .await()

        return try {
            firebaseFirestore.collection(GROUPS)
                .document(group.documents[0].id)
                .update(
                    mapOf(
                        "imageUrl" to groups.imageUrl,
                        "name" to groups.name,
                        "description" to groups.description
                    )
                )
                .await()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun startChat(
        users: Users,
        application: Application
    ): Chats {
        val isInMyChat = firebaseFirestore.collection(MY_CHATS)
            .document(application.getUsername())
            .collection(MY_CHATS)
            .whereEqualTo("username", users.username)
            .limit(1)
            .get()
            .await()

        var chats = Chats()

        if (isInMyChat.isEmpty) {
            chats.profileUrl = users.profileUrl
            chats.username = users.username
            firebaseFirestore.collection(MY_CHATS)
                .document(application.getUsername())
                .collection(MY_CHATS)
                .add(chats)
                .addOnSuccessListener {
                    chats.profileUrl = application.getProfileUrl()
                    chats.username = application.getUsername()
                    firebaseFirestore.collection(MY_CHATS)
                        .document(users.username)
                        .collection(MY_CHATS)
                        .add(chats)
                }
        } else {
            chats = isInMyChat.toObjects(Chats::class.java)[0]
        }

        return chats
    }

    @ExperimentalCoroutinesApi
    fun getPrivateChatMessages(roomId: String): Flow<Event<Resource<List<PrivateMessage>>>> {
        return channelFlow {
            val groupChats = firebaseFirestore.collection(PRIVATE_CHATS)
                .document(roomId)
                .collection(PRIVATE_CHATS)
                .limit(60)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(Event(Resource("error")))
                    } else {
                        trySend(
                            Event(
                                Resource(
                                    "success",
                                    value!!.toObjects(PrivateMessage::class.java)
                                )
                            )
                        )
                    }
                }
            awaitClose { groupChats.remove() }
        }
    }

    suspend fun sendPrivateMessage(
        roomId: String,
        username: String,
        privateMessage: PrivateMessage
    ) {
        val message = when (privateMessage.type) {
            0 -> privateMessage.message
            1 -> "Image"
            else -> "Video"
        }

        firebaseFirestore.collection(PRIVATE_CHATS)
            .document(roomId)
            .collection(PRIVATE_CHATS)
            .add(privateMessage)
            .await()

        firebaseFirestore.collection(MY_CHATS)
            .document(username)
            .collection(MY_CHATS)
            .whereEqualTo("username", privateMessage.username)
            .limit(1)
            .get()
            .addOnSuccessListener {
                var count = it.toObjects(Chats::class.java)[0].count

                count += 1

                firebaseFirestore.collection(MY_CHATS)
                    .document(username)
                    .collection(MY_CHATS)
                    .document(it.documents[0].id)
                    .update(
                        mapOf(
                            "message" to message,
                            "count" to count,
                            "timestamp" to Timestamp.now()
                        )
                    )
            }

        firebaseFirestore.collection(MY_CHATS)
            .document(privateMessage.username)
            .collection(MY_CHATS)
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .addOnSuccessListener {
                firebaseFirestore.collection(MY_CHATS)
                    .document(privateMessage.username)
                    .collection(MY_CHATS)
                    .document(it.documents[0].id)
                    .update(
                        mapOf(
                            "message" to message,
                            "count" to 0,
                            "timestamp" to Timestamp.now()
                        )
                    )
            }
    }

    fun markAsRead(
        username: String,
        friendName:String
    ){
        firebaseFirestore.collection(MY_CHATS)
            .document(username)
            .collection(MY_CHATS)
            .whereEqualTo("username", friendName)
            .limit(1)
            .get()
            .addOnSuccessListener {
                firebaseFirestore.collection(MY_CHATS)
                    .document(username)
                    .collection(MY_CHATS)
                    .document(it.documents[0].id)
                    .update(
                        mapOf(
                            "count" to 0,
                        )
                    )
            }
    }

    fun clearGroupMessage(roomId: String) {
        firebaseFirestore.collection(GROUP_CHATS)
            .document(roomId)
            .collection(GROUP_CHATS)
            .get()
            .addOnSuccessListener {
                for (doc in it) {
                    firebaseFirestore.collection(GROUP_CHATS)
                        .document(roomId)
                        .collection(GROUP_CHATS)
                        .document(doc.id)
                        .delete()
                }
            }
    }

}