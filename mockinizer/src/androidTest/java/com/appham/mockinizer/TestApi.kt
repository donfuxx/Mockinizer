package com.appham.mockinizer

import retrofit2.Call
import retrofit2.http.*

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

    @Headers(
        "name: value",
        "foo: bar"
    )
    @GET("headers")
    fun getMockedHeaders(): Call<Unit>

    @Headers("foo: bar")
    @GET("headersPartial")
    fun getMockedHeadersPartial(): Call<Unit>

    @Headers(
        "name: value",
        "foo: bar",
        "one: too much"
    )
    @GET("headersTooMany")
    fun getMockedHeadersTooMany(): Call<Unit>

    @Headers(
        "whatever: value"
    )
    @GET("headersAny")
    fun getMockedHeadersAny(): Call<Post>

    @Headers(
        "any: value",
        "something: else"
    )
    @GET("headersAny")
    fun getMockedHeadersAny2(): Call<Post>

    @GET("headersNone")
    fun getMockedHeadersNone(): Call<Post>

    @GET("query")
    fun getMockedQueryParam(@Query("param") param: String? = null): Call<Unit>

}
