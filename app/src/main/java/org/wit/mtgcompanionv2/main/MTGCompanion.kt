package org.wit.mtgcompanionv2.main

import android.app.Application
import com.google.firebase.FirebaseApp
import org.wit.mtgcompanionv2.models.CardDBStore
import org.wit.mtgcompanionv2.models.CardStore
import timber.log.Timber

class MTGCompanion : Application() {

    lateinit var cards: CardStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        cards = CardDBStore()
        cards.findAll()
        Timber.i("DonationX Application Started")
    }
}