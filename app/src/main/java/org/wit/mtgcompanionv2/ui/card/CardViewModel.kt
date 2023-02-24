package org.wit.mtgcompanionv2.ui.card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wit.mtgcompanionv2.models.CardManager
import org.wit.mtgcompanionv2.models.CardModel

class CardViewModel : ViewModel() {
    private val status = MutableLiveData<Boolean>()

    val observableStatus: LiveData<Boolean>
        get() = status

    fun addCard(card: CardModel){
        status.value = try {
            CardManager.create(card.copy())
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun updateCard(card: CardModel){
        status.value = try {
            CardManager.update(card)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun deleteCard(card: CardModel){
        status.value = try {
            CardManager.delete(card)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}