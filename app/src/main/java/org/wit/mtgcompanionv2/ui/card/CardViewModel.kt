package org.wit.mtgcompanionv2.ui.card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.wit.mtgcompanionv2.firebase.FirebaseDBManager
import org.wit.mtgcompanionv2.models.CardModel

class CardViewModel : ViewModel() {
    private val status = MutableLiveData<Boolean>()

    val observableStatus: LiveData<Boolean>
        get() = status

    fun addCard(firebaseUser: MutableLiveData<FirebaseUser>, card: CardModel){
        status.value = try {
            FirebaseDBManager.create(firebaseUser, card)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun updateCard(card: CardModel){
        status.value = try {
            //CardManager.update(card)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun deleteCard(card: CardModel){
        status.value = try {
            //CardManager.delete(card)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}