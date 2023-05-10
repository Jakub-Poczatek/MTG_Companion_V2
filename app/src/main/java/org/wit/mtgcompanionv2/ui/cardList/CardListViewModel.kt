package org.wit.mtgcompanionv2.ui.cardList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.wit.mtgcompanionv2.firebase.FirebaseDBManager
//import org.wit.mtgcompanionv2.models.CardManager
import org.wit.mtgcompanionv2.models.CardModel
import timber.log.Timber
import java.lang.Exception

class CardListViewModel : ViewModel() {
    private val cardsList = MutableLiveData<List<CardModel>>()

    val observableCardList: LiveData<List<CardModel>>
        get() = cardsList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init {
        load()
    }

    fun load() {
        try {
            FirebaseDBManager.findAll(liveFirebaseUser.value?.uid!!, cardsList)
            Timber.i("Cards List Load Success : ${cardsList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Cards List Load Error : $e.message")
        }
    }
}