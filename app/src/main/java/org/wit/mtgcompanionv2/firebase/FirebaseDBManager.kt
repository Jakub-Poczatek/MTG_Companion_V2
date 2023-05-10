package org.wit.mtgcompanionv2.firebase

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.wit.mtgcompanionv2.models.CardModel
import org.wit.mtgcompanionv2.models.CardStore
import timber.log.Timber

object FirebaseDBManager: CardStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun findAll(cardsList: MutableLiveData<List<CardModel>>) {
        TODO("Not yet implemented")
    }

    override fun findAll(userid: String, cardsList: MutableLiveData<List<CardModel>>) {
        TODO("Not yet implemented")
    }

    override fun findById(userId: String, cardId: String, card: MutableLiveData<CardModel>) {
        TODO("Not yet implemented")
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
        val cardValues = card.toMap()
        val childAdd = HashMap<String, Any>()
        childAdd["/cards/$key"] = cardValues
        childAdd["/user-cards/$uid/$key"] = cardValues
        database.updateChildren(childAdd)
    }

    override fun delete(userid: String, cardId: String) {
        TODO("Not yet implemented")
    }

    override fun update(userid: String, cardId: String, card: CardModel) {
        TODO("Not yet implemented")
    }
}