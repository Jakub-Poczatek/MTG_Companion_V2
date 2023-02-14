package org.wit.mtgcompanionv2.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardModel(
    var id: String = "0",
    var name: String = "SampleName",
    var type: String = "SampleType",
    var power: Short = 0,
    var toughness: Short = 0,
    var neutral: Short = 0,
    var white: Short = 0,
    var black: Short = 0,
    var red: Short = 0,
    var blue: Short = 0,
    var green: Short = 0,
    var description: String = "SampleDescription",
    var image: Uri = Uri.EMPTY
): Parcelable
