package org.wit.mtgcompanionv2.main

import android.app.Application
import org.wit.mtgcompanionv2.models.CardManager
import timber.log.Timber

class MTGCompanion : Application() {

    //lateinit var cards: CardStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        //cards = CardDBStore()
        //cards = CardManager()
        //cards.findAll()
        CardManager.findAll()
        Timber.i("DonationX Application Started")
    }
}