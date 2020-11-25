package com.example.carsharing.driver_license

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

class VerifyDriverLicenseTask(
    private var activity: Activity?,
    private val photoUri: Uri,
    private val uid: String
) : AsyncTask<String, String, String>() {

    override fun doInBackground(vararg params: String?): String? {
        Log.d(TAG, "start task")
        val imageStream = activity!!.contentResolver.openInputStream(photoUri)
        val bitmap = BitmapFactory.decodeStream(imageStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        val encodedImage: String = Base64.encodeToString(bytes, Base64.DEFAULT)

        val idAnalyzerAPI = IDAnalyzerAPI.getInstance()
        Log.d(TAG, "start api")
        idAnalyzerAPI.start(encodedImage, uid)

        return ""
    }

    companion object {
        private const val TAG = "VerifyDriverLicenseTask"
    }
}