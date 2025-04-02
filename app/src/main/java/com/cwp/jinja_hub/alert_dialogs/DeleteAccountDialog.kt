package com.cwp.jinja_hub.alert_dialogs

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cwp.jinja_hub.databinding.FragmentDeleteAccountDialogBinding
import com.cwp.jinja_hub.model.ADModel
import com.cwp.jinja_hub.model.ReviewModel
import com.cwp.jinja_hub.ui.client_registration.Login
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class DeleteAccountDialog : Fragment() {

    private var _binding: FragmentDeleteAccountDialogBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeleteAccountDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.delete.setOnClickListener {
            reauthenticateUser()
        }

        binding.cancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun reauthenticateUser() {
        binding.loaderLayout.visibility = View.VISIBLE
        binding.indicator.text = "Re-authenticating..."
        val user = auth.currentUser
        val email = user?.email
        if (email != null) {

            val credential = EmailAuthProvider.getCredential(email, binding.password.text.toString())
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.indicator.text = "Deleting account..."
                        deleteAccount()
                    } else {
                        Toast.makeText(requireContext(), " ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun deleteAccount() {
        binding.loaderLayout.visibility = View.VISIBLE // Show progress bar
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = auth.currentUser
                val userId = user?.uid

                if (userId != null) {
                    withContext(Dispatchers.Main){
                        binding.indicator.text = "Finalizing deleting account..."
                    }
                    deleteUserData(userId)
                    deleteAuthUser(user)
                    withContext(Dispatchers.Main) {
                        navigateToLogin()
                        Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.loaderLayout.visibility = View.GONE // Hide progress bar
                        Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("DeleteAccount", "Error deleting account: ${e.message}")
                    Toast.makeText(requireContext(), "Error deleting account: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.loaderLayout.visibility = View.GONE // Hide progress bar
                }
            }
        }
    }

    private suspend fun deleteUserData(userId: String) = withContext(Dispatchers.IO) {
        withContext(Dispatchers.Main) { // Switch to main thread for UI updates
            binding.loaderLayout.visibility = View.VISIBLE
            binding.indicator.text = "Deleting your data..."
        }
        database.reference.child("Users").child(userId).removeValue().await()
        withContext(Dispatchers.Main) {
            binding.indicator.text = "Deleting your chats..."
        }
        // database.reference.child("Chats").orderByChild("senderId").equalTo(userId).ref.removeValue().await()

        suspendCoroutine { continuation ->
            binding.indicator.text = "Deleting your Advert (Jinja Herbal Extract)..."
            database.reference.child("AD").child("Jinja Herbal Extract").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var adDeleted = 0
                    val totalAD = snapshot.childrenCount.toInt()
                    if (totalAD == 0) {
                        continuation.resume(Unit)
                        return
                    }
                    for (reviewSnapshot in snapshot.children) {
                        val ad = reviewSnapshot.getValue(ADModel::class.java)
                        if (ad != null && ad.posterId == userId) {
                            reviewSnapshot.ref.removeValue().addOnSuccessListener {
                                adDeleted++
                                if (adDeleted == totalAD) {
                                    continuation.resume(Unit)
                                }
                            }.addOnFailureListener {
                                continuation.resumeWithException(it)
                            }
                        } else {
                            continuation.resume(Unit)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DeleteAccount", "Reviews deletion cancelled: ${error.message}")
                    continuation.resumeWithException(error.toException())
                }
            })
        }


        suspendCoroutine { continuation ->
            binding.indicator.text = "Deleting your Advert (Iru Soap)..."
            database.reference.child("AD").child("Iru Soap").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var adDeleted = 0
                    val totalAD = snapshot.childrenCount.toInt()
                    if (totalAD == 0) {
                        continuation.resume(Unit)
                        return
                    }
                    for (reviewSnapshot in snapshot.children) {
                        val ad = reviewSnapshot.getValue(ADModel::class.java)
                        if (ad != null && ad.posterId == userId) {
                            reviewSnapshot.ref.removeValue().addOnSuccessListener {
                                adDeleted++
                                if (adDeleted == totalAD) {
                                    continuation.resume(Unit)
                                }
                            }.addOnFailureListener {
                                continuation.resumeWithException(it)
                            }
                        } else {
                            continuation.resume(Unit)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DeleteAccount", "Reviews deletion cancelled: ${error.message}")
                    continuation.resumeWithException(error.toException())
                }
            })
        }


        suspendCoroutine { continuation ->
            binding.indicator.text = "Deleting your Testimonials..."
            database.reference.child("Reviews").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var reviewsDeleted = 0
                    val totalReviews = snapshot.childrenCount.toInt()
                    if (totalReviews == 0) {
                        continuation.resume(Unit)
                        return
                    }
                    for (reviewSnapshot in snapshot.children) {
                        val review = reviewSnapshot.getValue(ReviewModel::class.java)
                        if (review != null && review.posterId == userId) {
                            reviewSnapshot.ref.removeValue().addOnSuccessListener {
                                reviewsDeleted++
                                if (reviewsDeleted == totalReviews) {
                                    continuation.resume(Unit)
//                                    withContext(Dispatchers.Main) {
//                                        binding.indicator.text = "Deleting your chatList.."
//                                    }
//                                    database.reference.child("ChatLists").child(userId).removeValue().addOnSuccessListener {
//                                        continuation.resume(Unit)
//                                    }.addOnFailureListener {
//                                        continuation.resumeWithException(it)
//                                    }
                                }
                            }.addOnFailureListener {
                                continuation.resumeWithException(it)
                            }
                        } else {
                            continuation.resume(Unit)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DeleteAccount", "Reviews deletion cancelled: ${error.message}")
                    continuation.resumeWithException(error.toException())
                }
            })
        }
    }


    private suspend fun deleteAuthUser(user: com.google.firebase.auth.FirebaseUser?) = withContext(Dispatchers.IO) {
        user?.delete()?.await()
    }

    private fun navigateToLogin() {
        Intent(requireActivity(), Login::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}