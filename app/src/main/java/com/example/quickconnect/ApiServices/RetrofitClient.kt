package com.example.quickconnect.ApiServices

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class RetrofitClient {


    companion object {

//        const val TESTING_URL = "http://65.0.110.5"
//        const val DEV_URL = "https://ec2-3-110-218-121.ap-south-1.compute.amazonaws.com/"
//        const val PROD_URL = "http://192.168.0.160:8002"
//        const val PROD_URL = "http://172.174.177.149"


        const val PROD_URL = "http://192.168.0.105:8001/"


        val  httpClient = OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        var retofit: Retrofit? = null
        val client: Retrofit
            get() {
                if (retofit == null) {

                    retofit = Retrofit.Builder()
                        .baseUrl(PROD_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(httpClient.build())
                        .build()
                }
                return retofit!!
            }

    }

    internal class NullOnEmptyConverterFactory : Converter.Factory() {
        fun responseBody(
            type: Type?,
            annotations: Array<Annotation?>?,
            retrofit: Retrofit
        ): Any {
            val delegate: Converter<ResponseBody, *> =
                retrofit.nextResponseBodyConverter<Any>(this, type, annotations)
            return object {
                fun convert(body: ResponseBody): Any? {
                    return if (body.contentLength() == 0L) null else delegate.convert(body)
                }
            }
        }
    }


}