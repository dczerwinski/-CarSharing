package com.example.carsharing.ui.payment_methods

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlin.collections.set

class PaymentMethodsViewModel : ViewModel() {
    private val _cards = HashMap<String, PaymentMethodsRecyclerViewAdapter.Item>()
    private val cards: MutableLiveData<HashMap<String, PaymentMethodsRecyclerViewAdapter.Item>> =
        MutableLiveData()
    private val uid = FirebaseAuth.getInstance().currentUser!!.uid

    fun remove(id: String) = viewModelScope.launch {
        val db = FirebaseDatabase.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        db.getReference("users_data")
            .child(uid)
            .child("cards")
            .child(id)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "Card deleted")
            }
            .addOnFailureListener {
                Log.w(TAG, "Filed card delete")
            }

    }

    fun updatePositions(id: String, map: Map<String, Any>) = viewModelScope.launch {
        Log.d("DOMINIK", "id = $id  mapa= $map")
        val db = FirebaseDatabase.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        db.getReference("users_data")
            .child(uid)
            .child("cards")
            .child(id)
            .updateChildren(map)
    }

    fun getCards(): LiveData<HashMap<String, PaymentMethodsRecyclerViewAdapter.Item>> {
        if (cards.value == null) {
            FirebaseDatabase.getInstance()
                .getReference("users_data")
                .child(uid)
                .child("cards")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        Log.d(TAG, "onChildAdded $snapshot")
                        val card = snapshot.getValue(
                            PaymentMethodsRecyclerViewAdapter.Item::class.java
                        )!!
                        val key = snapshot.key!!
                        _cards[key] = card
                        cards.postValue(_cards)
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        Log.d(TAG, "onChildChanged $snapshot")
                        val car = snapshot.getValue(
                            PaymentMethodsRecyclerViewAdapter.Item::class.java
                        )!!
                        val key = snapshot.key!!
                        _cards[key] = car
                        cards.postValue(_cards)
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        Log.d(TAG, "onChildRemoved $snapshot")
                        val key = snapshot.key!!
                        _cards.remove(key)
                        cards.postValue(_cards)
                    }

                    override fun onChildMoved(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "$error")
                    }
                })
        }
        return cards
    }

    companion object {
        private const val TAG = "PaymentMethodsViewModel"
    }
}