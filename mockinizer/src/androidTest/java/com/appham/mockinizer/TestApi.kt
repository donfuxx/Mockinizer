package com.appham.mockinizer

import retrofit2.Call
import retrofit2.http.GET

/**
 * This Api defines how to get demo data
 */
interface TestApi {

    @GET("posts")
    fun getPosts(): Call<List<Post>>

    @GET("error500")
    fun getMockedError500(): Call<Unit>

}
