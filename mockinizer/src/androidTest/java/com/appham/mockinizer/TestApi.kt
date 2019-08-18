package com.appham.mockinizer

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * This Api defines how to get demo data
 */
interface TestApi {

    @GET("posts")
    fun getPosts(): Call<List<Post>>

    @GET("error500")
    fun getMockedError500(): Call<Unit>

    @DELETE("delete")
    fun getMockedDelete(): Call<Unit>

    @POST("post")
    fun getMockedPost(@Body post: Post): Call<Post>

}
