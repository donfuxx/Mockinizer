package com.appham.mockinizer

import io.reactivex.Single
import retrofit2.http.GET

/**
 * This Api defines how to get demo data
 */
interface DemoApi {

    @GET("posts")
    fun getPosts(): Single<List<Post>>

    @GET("mocked")
    fun getMocked(): Single<List<Post>>

}
