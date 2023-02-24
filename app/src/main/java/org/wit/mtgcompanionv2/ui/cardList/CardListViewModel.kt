package org.wit.mtgcompanionv2.ui.cardList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wit.mtgcompanionv2.models.CardManager
import org.wit.mtgcompanionv2.models.CardModel

class CardListViewModel : ViewModel() {
    private val cardsList = MutableLiveData<List<CardModel>>()

    val observableCardList: LiveData<List<CardModel>>
        get() = cardsList

    init {
        load()
    }

    fun load() {
        cardsList.value = CardManager.findAll()
    }
}