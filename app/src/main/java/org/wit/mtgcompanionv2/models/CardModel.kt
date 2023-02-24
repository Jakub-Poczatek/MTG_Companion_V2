package org.wit.mtgcompanionv2.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardModel(
    var id: String = "",
    var name: String = "",
    var type: String = "",
    var power: Short = -1,
    var toughness: Short = -1,
    var neutral: Short = -1,
    var white: Short = -1,
    var black: Short = -1,
    var red: Short = -1,
    var blue: Short = -1,
    var green: Short = -1,
    var description: String = "",
    var image: Uri = Uri.EMPTY
): Parcelable
