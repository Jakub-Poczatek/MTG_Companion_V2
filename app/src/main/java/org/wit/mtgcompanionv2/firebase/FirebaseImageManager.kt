package org.wit.mtgcompanionv2.firebase

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import org.wit.mtgcompanionv2.utils.customTransformation
import timber.log.Timber
import java.io.ByteArrayOutputStream
import com.squareup.picasso.Target
import java.util.Dictionary

object FirebaseImageManager {
    var storage = FirebaseStorage.getInstance().reference
    var userImageUri = MutableLiveData<Uri>()
    var cardArtUris = MutableLiveData<Dictionary<String, Uri>>()
    public val pathToPhotos: String = "${storage}photos/xVwdDl1dnEQLqXkrLs8g7vKkxCh2/"

    fun checkStorageForExistingProfilePic(userid: String) {
        val imageRef = storage.child("photos").child(userid).child("profilePic.jpg")
        val defaultImageRef = storage.child("homer.jpg")

        imageRef.metadata.addOnSuccessListener { //File Exists
            imageRef.downloadUrl.addOnCompleteListener { task ->
                userImageUri.value = task.result!!
            }
            //File Doesn't Exist
        }.addOnFailureListener {
            userImageUri.value = Uri.EMPTY
        }
    }

//    fun checkStorageForExistingCardArt(userid: String, cardId: String): Uri {
//        val imageRef = storage.child("photos").child(userid).child("${cardId}.jpg")
//        var tempCardArt = MutableLiveData<Uri>()
//
//        imageRef.metadata.addOnSuccessListener {
//            imageRef.downloadUrl.addOnCompleteListener { task ->
//                Timber.i("Completed this thing now!!!!!!!!!!!")
//            }
//        }.addOnFailureListener{
//            tempCardArt.value = Uri.EMPTY
//        }
//    }


    fun uploadImageToFirebase(userid: String, bitmap: Bitmap, updating : Boolean, cardId: String = "profilePic") {
        // Get the data from an ImageView as bytes
        val imageRef = storage.child("photos").child(userid).child("${cardId}.jpg")
        Timber.i("This is the storage to String: " + storage.path)
        //val bitmap = (imageView as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        lateinit var uploadTask: UploadTask

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.metadata.addOnSuccessListener { //File Exists
            if(updating) // Update existing Image
            {
                uploadTask = imageRef.putBytes(data)
                uploadTask.addOnSuccessListener { ut ->
                    ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                        userImageUri.value = task.result!!
                    }
                }
            }
        }.addOnFailureListener { //File Doesn't Exist
            uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener { ut ->
                ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                    userImageUri.value = task.result!!
                }
            }
        }
    }

    fun updateUserImage(userid: String, imageUri : Uri?, imageView: ImageView, updating : Boolean) {
        Picasso.get().load(imageUri)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {
                    Timber.i("MTGComp onBitmapLoaded $bitmap")
                    uploadImageToFirebase(userid, bitmap!!,updating)
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                    Timber.i("MTGComp onBitmapFailed $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

    fun updateDefaultImage(userid: String, resource: Int, imageView: ImageView) {
        Picasso.get().load(resource)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {
                    Timber.i("MTGComp onBitmapLoaded $bitmap")
                    uploadImageToFirebase(userid, bitmap!!,false)
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                    Timber.i("MTGComp onBitmapFailed $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

    fun updateCardArt(userid: String, cardId: String, uri: Uri){
        Picasso.get().load(uri)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {
                    Timber.i("MTGComp onBitmapLoaded $bitmap")
                    uploadImageToFirebase(userid, bitmap!!,false, cardId)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                    Timber.i("MTGComp onBitmapFailed $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }
}