package org.wit.mtgcompanionv2.models

import androidx.core.net.toUri
//import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber.e
import timber.log.Timber.i
import java.util.concurrent.TimeUnit

class CardDBStore: CardStore{

    private var dbConnected = false
    private val db = Firebase.firestore
    //private val auth = Firebase.auth
    private val cards = ArrayList<CardModel>()
    private val user = "defaultUser"

    override fun findAll(): List<CardModel> {
        if(dbConnected) return cards
        else {
            cards.clear()
            //val user = auth.currentUser?.email.toString()
            val result = db.collection(user).get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val card = CardModel(
                            id = document.id,
                            name = document.getString("name")!!,
                            type = document.getString("type")!!,
                            power = document.get("power").toString().toShort(),
                            toughness = document.get("toughness").toString().toShort(),
                            neutral = document.get("neutral").toString().toShort(),
                            white = document.get("white").toString().toShort(),
                            black = document.get("black").toString().toShort(),
                            red = document.get("red").toString().toShort(),
                            blue = document.get("blue").toString().toShort(),
                            green = document.get("green").toString().toShort(),
                            description = document.getString("description")!!,
                            image = document.getString("image")!!.toUri()
                        )
                        cards.add(card)
                    }
                    cards.forEach {
                        i("$it")
                    }
                }
                .addOnFailureListener { error ->
                    e("Error getting card: \n$error")
                }

            fun myWait() {
                if (!result.isComplete) {
                    TimeUnit.MILLISECONDS.sleep(500)
                    i("Still Running")
                    myWait()
                } else return
            }
            myWait()
            dbConnected = true
            return cards
        }
    }

    override fun create(card: CardModel) {
        //val user = auth.currentUser?.email.toString()
        db.collection(user).add(cardToHash(card))
            .addOnSuccessListener { documentRef ->
                i("Card added with ID: ${documentRef.id}")
                val foundCard: CardModel? = cards.find { c -> c.id == card.id }
                foundCard?.id = documentRef.id

            }
            .addOnFailureListener { error ->
                e("Error adding card: \n$error")
            }
        cards.add(card)
    }

    override fun update(card: CardModel) {
        //val user = auth.currentUser?.email.toString()
        db.collection(user).document(card.id).set(cardToHash(card))
            .addOnSuccessListener {
                i("Card successfully updated: $card")
            }
            .addOnFailureListener{ error ->
                e("Error updating card: $error")
            }
        val foundCard: CardModel? = cards.find { c -> c.id == card.id }
        if(foundCard != null){
            foundCard.name = card.name
            foundCard.type = card.type
            foundCard.power = card.power
            foundCard.toughness = card.toughness
            foundCard.neutral = card.neutral
            foundCard.white = card.white
            foundCard.black = card.black
            foundCard.red = card.red
            foundCard.blue = card.blue
            foundCard.green = card.green
            foundCard.description = card.description
            foundCard.image = card.image
        }
    }

    override fun delete(card: CardModel) {
        //val user = auth.currentUser?.email.toString()
        db.collection(user).document(card.id).delete()
            .addOnSuccessListener {
                i("Card successfully deleted!")
            }
            .addOnFailureListener { error ->
                e("Error deleting card: $error")
            }
        cards.remove(card)
    }

    private fun cardToHash(card: CardModel): HashMap<String, Comparable<*>> {
        return hashMapOf(
            "name" to card.name,
            "type" to card.type,
            "power" to card.power.toInt(),
            "toughness" to card.toughness.toInt(),
            "neutral" to card.neutral.toInt(),
            "white" to card.white.toInt(),
            "black" to card.black.toInt(),
            "red" to card.red.toInt(),
            "blue" to card.blue.toInt(),
            "green" to card.green.toInt(),
            "description" to card.description,
            "image" to card.image
        )
    }
}