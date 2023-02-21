package org.wit.mtgcompanionv2.ui.map

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaceModel(
    var id: Short = 0,
    var name: String ="",
    var address: String = "",
    var loc: LatLng = LatLng(0.0, 0.0),
    var rating: Double = 0.0,
    var totalUserRatings: Int,
    var open: Boolean = false
): Parcelable
