package com.appham.mockinizer

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import com.jakewharton.espresso.OkHttp3IdlingResource
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class MockWebServerTest {

    private val okHttpClient: OkHttpClient by lazy {
        okHttpClientBuilder.build()
    }

    private val okHttpClientBuilder: OkHttpClient.Builder by lazy {
        OkHttpClient.Builder()
    }

    inner class OkHttpIdlingResourceRule : TestRule {

        private val resource: IdlingResource = OkHttp3IdlingResource.create(
            "okhttp",
            okHttpClient
        )

        override fun apply(base: Statement, description: Description): Statement {
            return object : Statement() {
                override fun evaluate() {
                    IdlingRegistry.getInstance().register(resource)
                    base.evaluate()
                    IdlingRegistry.getInstance().unregister(resource)
                }
            }
        }
    }

    private fun createRetrofit(mockWebServer: MockWebServer = MockWebServer().configure()): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client =
            okHttpClientBuilder.addInterceptor(interceptor).mockinize(mocks, mockWebServer).build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @get:Rule
    var rule = OkHttpIdlingResourceRule()

    @Test
    fun testStartStopStaticMockServer() {
        val mockServer = MockWebServer().configure()
        var api = createRetrofit(mockServer).create(TestApi::class.java)
        api.getPosts().execute()
        assertThat(mockServer.port).isEqualTo(34567)

        Mockinizer.shutDown()
        assertThat(mockServer.port).isEqualTo(34567)

        api = createRetrofit(mockServer).create(TestApi::class.java)
        api.getPosts().execute()
        assertThat(mockServer.port).isEqualTo(34567)
    }

    @Test
    fun testStartStopCustomMockServer() {
        val mockServer = MockWebServer().configure()
        var api = createRetrofit(mockServer).create(TestApi::class.java)
        api.getPosts().execute()
        assertThat(mockServer.port).isEqualTo(34567)

        mockServer.shutdown()
        assertThat(mockServer.port).isEqualTo(34567)

        api = createRetrofit(mockServer).create(TestApi::class.java)
        api.getPosts().execute()
        assertThat(mockServer.port).isEqualTo(34567)
    }

    @After
    fun tearDown() {
        Mockinizer.shutDown()
    }
}
