package com.cwp.jinja_hub.repository

import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.JinjaDrinkCardItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JinjaMarketRepository {
    private val fUser = FirebaseAuth.getInstance().currentUser
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("jinja_drinks")
    private val userLikedRef: DatabaseReference = databaseReference.child("user_likes")
    private var drinksListener: ValueEventListener? = null


    init {
        // Enable offline syncing for these nodes
        databaseReference.keepSynced(true)
        userLikedRef.keepSynced(true)
    }

    fun likeJinjaDrink(
        jinjaDrinkId: String,
        like: Boolean,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = fUser
        if (currentUser == null) {
            onFailure("User not authenticated")
            return
        }

        val userLikeRef = userLikedRef.child(currentUser.uid).child(jinjaDrinkId)

        if (like) {
            userLikeRef.setValue(true)
                .addOnSuccessListener {
                    onSuccess("Liked successfully")
                }
                .addOnFailureListener {
                    onFailure("Failed to like: ${it.message}")
                }
        } else {
            userLikeRef.removeValue()
                .addOnSuccessListener {
                    onSuccess("Unliked successfully")
                }
                .addOnFailureListener {
                    onFailure("Failed to unlike: ${it.message}")
                }
        }
    }


    fun fetchJinjaDrinks(onSuccess: (List<JinjaDrinkCardItem>) -> Unit, onFailure: (Exception) -> Unit) {
        databaseReference.keepSynced(true)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jinjaDrinks = mutableListOf<JinjaDrinkCardItem>()

                val seenItems = mutableSetOf<String>() // Track unique IDs

                for (drinkSnapshot in snapshot.children) {
                    val drink = drinkSnapshot.getValue(JinjaDrinkCardItem::class.java)
                    if (drink != null && seenItems.add(drink.id.toString())) {
                        // Only add items with unique IDs
                        jinjaDrinks.add(drink)
                    }
                }
                onSuccess(jinjaDrinks)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.toException())
            }
        })
    }



    fun generateCards(onSuccess: (List<JinjaDrinkCardItem>) -> Unit, onFailure: (Exception) -> Unit): List<JinjaDrinkCardItem> {
        val cards = mutableListOf<JinjaDrinkCardItem>()
        for (i in 1..10) {
            cards.add(
                JinjaDrinkCardItem(
                    id = i,
                    title = "Jinja $i",
                    oldPrice = "$10.00",
                    newPrice = "$5.00",
                    imageResId = R.drawable.shoe
                )
            )
            if (cards.isNotEmpty()) {
                onSuccess(cards)
            } else {
                onFailure(Exception("Failed to generate cards"))
            }
        }
        return cards
    }
}