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
    var neutral: Short = 0,
    var white: Short = 0,
    var black: Short = 0,
    var red: Short = 0,
    var blue: Short = 0,
    var green: Short = 0,
    var description: String = "",
    var rarity: Rarity = Rarity.Common,
    var colours: MutableList<Colour> = ArrayList<Colour>(),
    var set: String = "",
    var value: Double = -1.0,
    var image: Uri = Uri.EMPTY
): Parcelable

enum class Rarity{
    Common,
    Uncommon,
    Rare,
    Mythic
}

enum class Colour{
    Neutral,
    White,
    Black,
    Red,
    Blue,
    Green
}
