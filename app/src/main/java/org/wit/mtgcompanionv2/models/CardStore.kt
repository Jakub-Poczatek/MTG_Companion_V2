package org.wit.mtgcompanionv2.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface CardStore {
    fun findAll(cardsList: MutableLiveData<List<CardModel>>)
    fun findAll(userid: String, cardsList: MutableLiveData<List<CardModel>>)
    fun findById(userId: String, cardId: String, card: MutableLiveData<CardModel>)
    fun create(firebaseUser: MutableLiveData<FirebaseUser>, card: CardModel)
    fun delete(userid: String, cardId: String)
    fun update(userid: String, cardId: String, card: CardModel)
}