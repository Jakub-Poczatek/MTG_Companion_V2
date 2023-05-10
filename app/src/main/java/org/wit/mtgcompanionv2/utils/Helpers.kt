package org.wit.mtgcompanionv2.utils

import android.app.AlertDialog
import android.graphics.Color
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Transformation
import org.wit.mtgcompanionv2.R

fun createLoader(activity: FragmentActivity) : AlertDialog {
    val loaderBuilder = AlertDialog.Builder(activity)
        .setCancelable(true)
        .setView(R.layout.loading)
    var loader = loaderBuilder.create()
    loader.setTitle("MTG Companion")
    loader.setIcon(R.mipmap.ic_launcher_round)
    return loader
}

fun showLoader(loader: AlertDialog, message: String){
    if (!loader.isShowing) {
        loader.setTitle(message)
        loader.show()
    }
}

fun hideLoader(loader: AlertDialog) {
    if (loader.isShowing)
        loader.dismiss()
}

fun serviceUnavailableMessage(activity: FragmentActivity) {
    Toast.makeText(
        activity,
        "MTG Companion Service Unavailable. Try Again Later",
        Toast.LENGTH_LONG
    ).show()
}

fun serviceAvailableMessage(activity: FragmentActivity) {
    Toast.makeText(
        activity,
        "Card MTG Companion Contacted Successfully",
        Toast.LENGTH_LONG
    )
}

fun customTransformation() : Transformation =
    RoundedTransformationBuilder()
        .borderColor(Color.WHITE)
        .borderWidthDp(2F)
        .cornerRadiusDp(35F)
        .oval(false)
        .build()
