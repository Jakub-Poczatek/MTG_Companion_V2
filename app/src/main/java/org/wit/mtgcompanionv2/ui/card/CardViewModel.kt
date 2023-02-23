package org.wit.mtgcompanionv2.ui.card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CardViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is the Card Fragment"
    }
    val text: LiveData<String> = _text
}