package org.wit.mtgcompanionv2.ui.card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.wit.mtgcompanionv2.firebase.FirebaseDBManager
import org.wit.mtgcompanionv2.models.CardModel
import timber.log.Timber

class CardViewModel : ViewModel() {
    private val card = MutableLiveData<CardModel>()
    private val status = MutableLiveData<Boolean>()

    val observableStatus: LiveData<Boolean>
        get() = status

    var observableCard: LiveData<CardModel>
        get() = card
        set(value) {card.value = value.value}

    fun getCard(userId: String, id: String){
        try{
            FirebaseDBManager.findById(userId, id, card)
            Timber.i("Detail getCard() Success: ${
                card.value.toString()}")
        } catch (e: Exception){
            Timber.i("Detail getCard() Error: ${e.message}")
        }
    }

    fun updateCard(userid: String, id: String, card: CardModel){
        try {
            FirebaseDBManager.update(userid, id, card)
            Timber.i("Card update() Success: $card")
        } catch (e: Exception) {
            Timber.i("Card update() Error: ${e.message}")
        }
    }

    fun addCard(firebaseUser: MutableLiveData<FirebaseUser>, card: CardModel){
        status.value = try {
            FirebaseDBManager.create(firebaseUser, card)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}