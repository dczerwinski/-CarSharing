package com.example.carsharing.driver_license

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class UploadVerificationDataTask(
    private val uid: String,
    private val responseFromApi: IDAnalyzerAPI.ResponseFromApi
) :
    AsyncTask<String, String, String>() {

    private lateinit var db: FirebaseDatabase

    override fun onPreExecute() {
        super.onPreExecute()
        db = FirebaseDatabase.getInstance()
    }

    @SuppressLint("LongLogTag")
    override fun doInBackground(vararg params: String?): String {

        val dataMap = HashMap<String, Any>()

        dataMap["fullName"] = responseFromApi.result.fullName
        dataMap["verified"] = true
        db.getReference("users_data")
            .child(uid)
            .setValue(dataMap)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added")
            }
            .addOnFailureListener {
                Log.w(TAG, "Error adding document", it)
            }

        return ""
    }

    companion object {
        private const val TAG = "UploadVerificationDataTask"
    }
}