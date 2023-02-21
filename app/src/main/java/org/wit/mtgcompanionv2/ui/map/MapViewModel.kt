package org.wit.mtgcompanionv2.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is the Map Fragment"
    }
    val text: LiveData<String> = _text
}