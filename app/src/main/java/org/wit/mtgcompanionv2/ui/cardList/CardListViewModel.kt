package org.wit.mtgcompanionv2.ui.cardList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CardListViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is the Card List Fragment"
    }
    val text: LiveData<String> = _text
}