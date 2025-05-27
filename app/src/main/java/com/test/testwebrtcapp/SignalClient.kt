package com.test.testwebrtcapp

import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import org.webrtc.IceCandidate

class SignalClient {

    private val database = Firebase.firestore

    fun sendOffer(description: String) {
        database.collection("offers").document("offers").set(mapOf("s" to description))
    }

    fun checkOffers(callback: (String?) -> Unit) {
        database.collection("offers").document("offers").get(Source.SERVER).addOnSuccessListener {
            val description = it?.data?.get("s") as String?
            callback(description)
        }
    }

    fun sendAnswer(description: String) {
        database.collection("answers").document("answers").set(mapOf("s" to description))
    }

    fun listenForAnswers(callback: (String) -> Unit) {
        database.collection("answers").document("answers").addSnapshotListener { value, error ->
            if (value?.data != null && error == null) {
                val description = value.data!!["s"] as String? ?: return@addSnapshotListener
                callback(description)
            }
        }
    }

    fun addIceCandidate(candidate: IceCandidate) {
        val str = Gson().toJson(candidate)
        val rnd = (0..10000).random().toString()
        database.collection("ice").document("ice").set(mapOf(rnd to str), SetOptions.merge())
    }

    fun listenForIce(callback: (IceCandidate) -> Unit) {
        database.collection("ice").document("ice").addSnapshotListener { value, error ->
            if (value?.data != null && error == null) {
                for (i in value.data!!.values) {
                    i as String
                    val candidate = Gson().fromJson(i, IceCandidate::class.java)
                    callback(candidate)
                }
            }
        }
    }

    fun close() {
        database.collection("offers").document("offers").set(emptyMap<String,String>())
        database.collection("answers").document("answers").set(emptyMap<String,String>())
        database.collection("ice").document("ice").set(emptyMap<String,String>())
    }

}