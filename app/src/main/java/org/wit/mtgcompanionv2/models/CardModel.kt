package org.wit.mtgcompanionv2.models

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardModel(
    var uid: String? = "",
    var name: String = "",
    var type: String = "",
    var power: Int = -1,
    var toughness: Int = -1,
    var neutral: Int = 0,
    var white: Int = 0,
    var black: Int = 0,
    var red: Int = 0,
    var blue: Int = 0,
    var green: Int = 0,
    var description: String = "",
    var rarity: Rarity = Rarity.Common,
    var colours: MutableList<Colour> = ArrayList<Colour>(),
    var set: String = "",
    var value: Double = -1.0,
    var image: String = "",
    var email: String? = ""
): Parcelable{

    @Exclude
    fun toMap(): Map<String, Any?>{
        return mapOf(
            "uid" to uid,
            "name" to name,
            "type" to type,
            "power" to power,
            "toughness" to toughness,
            "neutral" to neutral,
            "white" to white,
            "black" to black,
            "red" to red,
            "blue" to blue,
            "green" to green,
            "description" to description,
            "rarity" to rarity,
            "colours" to colours,
            "set" to set,
            "value" to value,
            "image" to image,
            "email" to email
        )
    }
}

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
