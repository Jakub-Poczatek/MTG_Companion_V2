package org.wit.mtgcompanionv2.main

import android.app.Application
import org.wit.mtgcompanionv2.models.CardMemStore
import org.wit.mtgcompanionv2.models.CardStore
import timber.log.Timber

class MTGCompanion : Application() {

    lateinit var cardStore: CardStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        cardStore = CardMemStore()
        Timber.i("DonationX Application Started")
    }
}