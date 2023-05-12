package org.wit.mtgcompanionv2.ui.cardList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.wit.mtgcompanionv2.firebase.FirebaseDBManager
import org.wit.mtgcompanionv2.firebase.FirebaseImageManager
import org.wit.mtgcompanionv2.firebase.SortTerm
//import org.wit.mtgcompanionv2.models.CardManager
import org.wit.mtgcompanionv2.models.CardModel
import timber.log.Timber
import java.lang.Exception

class CardListViewModel : ViewModel() {
    private val cardsList = MutableLiveData<List<CardModel>>()
    var liveFirebaseUser = MutableLiveData<FirebaseUser>()
    var readOnly = MutableLiveData(false)

    val observableCardList: LiveData<List<CardModel>>
        get() = cardsList


    init {
        load(SortTerm.None)
    }

    fun load(sortTerm: SortTerm) {
        try {
            readOnly.value = false
            FirebaseDBManager.findAll(liveFirebaseUser.value?.uid!!, cardsList, sortTerm)
            Timber.i("Cards List Load Success : ${cardsList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Cards List Load Error : $e.message")
        }
    }

    fun delete(userid: String, id: String){
        try{
            FirebaseDBManager.delete(userid, id)
            FirebaseImageManager.deleteCardArt(userid, id)
            Timber.i("Card Delete Success")
        } catch (e: Exception) {
            Timber.i("Card Delete Error: ${e.message}")
        }
    }

    fun loadAll(){
        try {
            readOnly.value = true
            FirebaseDBManager.findAll(cardsList)
            Timber.i("Card List LoadAll Success: ${cardsList.value.toString()}")
        } catch (e: Exception) {
            Timber.i("Card List LoadAll Error: ${e.message}")
        }
    }
}