package org.wit.mtgcompanionv2.firebase

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import org.wit.mtgcompanionv2.models.CardModel
import org.wit.mtgcompanionv2.models.CardStore
import timber.log.Timber

object FirebaseDBManager: CardStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun findAll(cardsList: MutableLiveData<List<CardModel>>) {
        database.child("cards")
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<CardModel>()
                    val children = snapshot.children
                    children.forEach{
                        val card = it.getValue(CardModel::class.java)
                        localList.add(card!!)
                    }
                    database.child("cards")
                        .removeEventListener(this)

                    cardsList.value = localList
                }
            })
    }

    override fun findAll(userid: String, cardsList: MutableLiveData<List<CardModel>>) {
        database.child("user-cards").child(userid)
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Card error: ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<CardModel>()
                    val children = snapshot.children
                    children.forEach {
                        val card = it.getValue(CardModel::class.java)
                        localList.add(card!!)
                    }
                    database.child("user-cards").child(userid)
                        .removeEventListener(this)

                    cardsList.value = localList
                }
            })
    }

    override fun findById(userId: String, cardId: String, card: MutableLiveData<CardModel>) {
        database.child("user-cards").child(userId).child(cardId).get().addOnSuccessListener {
            card.value = it.getValue(CardModel::class.java)
            Timber.i("Firebase got value: ${it.value}")
        }.addOnFailureListener{
            Timber.e("Firebase error getting data: $it")
        }
    }

    override fun create(firebaseUser: MutableLiveData<FirebaseUser>, card: CardModel) {
        Timber.i("Firebase DB Reference: $database")

        val uid = firebaseUser.value!!.uid
        val key = database.child("cards").push().key
        if (key == null) {
            Timber.i("Firebase Error: Key Empty")
            return
        }
        card.uid = key
        card.image = "${FirebaseImageManager.pathToPhotos}$key.jpg"
        Timber.i(card.image)
        val cardValues = card.toMap()
        val childAdd = HashMap<String, Any>()
        childAdd["/cards/$key"] = cardValues
        childAdd["/user-cards/$uid/$key"] = cardValues
        database.updateChildren(childAdd)
    }

    override fun delete(userid: String, cardId: String) {
        val childDelete: MutableMap<String, Any?> = HashMap()
        childDelete["/cards/$cardId"] = null
        childDelete["/user-cards/$userid/$cardId"] = null
        database.updateChildren(childDelete)
    }

    override fun update(userid: String, cardId: String, card: CardModel) {
        val cardValues = card.toMap()
        val childUpdate: MutableMap<String, Any?> = HashMap()
        childUpdate["/cards/$cardId"] = cardValues
        childUpdate["/user-cards/$userid/$cardId"] = cardValues
        database.updateChildren(childUpdate)
    }

    fun updateCardArtRef(userId: String, cardId: String){
        val userCards = database.child("user-cards").child(userId)
        val allCards = database.child("cards")

        userCards.addListenerForSingleValueEvent(
            object: ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach{

                    }
                }
            }
        )
    }
}