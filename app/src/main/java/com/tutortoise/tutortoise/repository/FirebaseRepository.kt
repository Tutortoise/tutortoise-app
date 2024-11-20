package com.tutortoise.tutortoise.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun createUser(email: String, password: String, name: String, callback: (Boolean, FirebaseUser?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        updateUserDisplayName(user, name) { success ->
                            if (success) {
                                saveUserToFirestore(user, name) { firestoreSuccess ->
                                    callback(firestoreSuccess, user)
                                }
                            } else {
                                callback(false, null)
                            }
                        }
                    } else {
                        callback(false, null)
                    }
                } else {
                    Log.w("FirebaseRepository", "createUserWithEmail:failure", task.exception)
                    callback(false, null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseRepository", "Error creating user", exception)
                callback(false, null)
            }
    }

    private fun updateUserDisplayName(user: FirebaseUser, name: String, callback: (Boolean) -> Unit) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseRepository", "User displayName updated")
                    callback(true)
                } else {
                    Log.e("FirebaseRepository", "Failed to update displayName", task.exception)
                    callback(false)
                }
            }
    }


    private fun saveUserToFirestore(user: FirebaseUser, name: String, callback: (Boolean) -> Unit) {
        val userRef = firestore.collection("users").document(user.uid)
        val userMap = hashMapOf(
            "name" to name,
            "email" to user.email,
            "uid" to user.uid,
            "createdAt" to System.currentTimeMillis()
        )

        userRef.set(userMap)
            .addOnSuccessListener {
                Log.d("FirebaseRepository", "User data saved to Firestore")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseRepository", "Error saving user data to Firestore", e)
                callback(false)
            }
    }
}
