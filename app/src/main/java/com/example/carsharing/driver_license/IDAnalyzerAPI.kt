package com.example.carsharing.driver_license

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class IDAnalyzerAPI {

    companion object {
        private const val TAG = "IDAnalyzerAPI"
        private const val API_KEY = "5sCnrJwwELtiVX9T8rXgClFpAMjLclou"

        @Volatile
        private var INSTANCE: IDAnalyzerAPI? = null

        fun getInstance(): IDAnalyzerAPI {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = IDAnalyzerAPI()
                INSTANCE = instance
                return instance
            }
        }
    }

    fun start(frontImageEncoded: String, uid: String) {
        val service = IDAnalyzerAPIServiceBuilder.buildService(IDAnalyzerAPIService::class.java)
        val requestCall = service.getResponse(API_KEY, frontImageEncoded)

        requestCall.enqueue(object : Callback<ResponseFromApi> {
            override fun onResponse(
                call: Call<ResponseFromApi>,
                response: Response<ResponseFromApi>
            ) {
                if (response.isSuccessful) {
                    val responseFromApi = response.body()!!
                    UploadVerificationDataTask(uid, responseFromApi).execute()
                }
            }

            override fun onFailure(call: Call<ResponseFromApi>, t: Throwable) {
                Log.e(TAG, t.toString())
            }
        })
    }

    private interface IDAnalyzerAPIService {

        @FormUrlEncoded
        @POST("/")
        fun getResponse(
            @Field("apikey") apiKey: String,
            @Field("file_base64") file: String,
        ): Call<ResponseFromApi>
    }

    private object IDAnalyzerAPIServiceBuilder {

        private const val URL = "https://api.idanalyzer.com"
        private val okHttp = OkHttpClient.Builder()
        private val builder = Retrofit.Builder().baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttp.build())
        private val retrofit = builder.build()

        fun <T> buildService(serviceType: Class<T>): T {
            return retrofit.create(serviceType)
        }
    }

    data class ResponseFromApi(
        val credit: Int,
        val executionTime: Double,
        val matchrate: Double,
        val quota: Int,
        val responseID: String,
        val result: Result,
        val vaultid: String
    ) {
        data class Result(
            val address1: String,
            val address2: String,
            val age: Int,
            val daysFromIssue: Int,
            val daysToExpiry: Int,
            val dob: String,
            val dob_day: Int,
            val dob_month: Int,
            val dob_year: Int,
            val documentNumber: String,
            val documentSide: String,
            val documentType: String,
            val expiry: String,
            val expiry_day: Int,
            val expiry_month: Int,
            val expiry_year: Int,
            val firstName: String,
            val fullName: String,
            val internalId: String,
            val issueAuthority: String,
            val issued: String,
            val issued_day: Int,
            val issued_month: Int,
            val issued_year: Int,
            val issuerOrg_full: String,
            val issuerOrg_iso2: String,
            val issuerOrg_iso3: String,
            val lastName: String,
            val nationality_full: String,
            val nationality_iso2: String,
            val nationality_iso3: String,
            val placeOfBirth: String
        )
    }
}