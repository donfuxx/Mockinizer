# Mockinizer [![Build Status](https://travis-ci.org/donfuxx/Mockinizer.svg?branch=master)](https://travis-ci.org/donfuxx/Mockinizer)

An [OkHttpClient](https://github.com/square/okhttp) / [RetroFit](https://github.com/square/retrofit) web api call mocking library that uses [MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver) to provide mocked responses by using Interceptors.

## What is it?

Mockinizer helps Android developers to build apps with web api calls that use OkHttpClient / RetroFit by allowing them to quickly mock some web api responses with MockWebServer. Mockinizer allows to mock only specific api calls, while other api calls will still call the original server. 
This is particularily usefull in the following scenarios:
- You are working on a new feature that needs to call new not yet existing apis. With Mockinizer you can quickly swap in the new desired api responses while other api calls will still use the real server
- You want to test error cases for existing apis. With Mockinizer you can can quickly mock an error 500 response or an 401 Unauthorized for selected api requests and verify if your app handles them gracefully
- You want to call a mocked api for unit testing and isolate those from the webserver

## Setup

### 1. Add jitpack.io repository: 
Add jitpack.io repository in **root build.gradle**:
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

### 2. Add Mockinizer gradle dependency
Add the below code in the **app module's build.gradle** (Usually you want to implement it only in debug builds and not release builds) At the time of writing the latest mockinizer_version was 1.1.0, you can get **latest release** version here: https://github.com/donfuxx/Mockinizer/releases
```gradle
dependencies {
    debugImplementation "com.github.donfuxx:Mockinizer:1.1.0"
}
``` 
You may also need to add a MockWebServer dependency in your app module:
```gradle
dependencies {
    implementation "com.squareup.okhttp3:mockwebserver:4.0.1"
}
``` 

### 3. Define the RequestFilter / MockResponse Pairs 
Those represent each api call that you want to mock. The **RequestFilter** defines the Request Path (relative to the RetroFit Baseurl) and/or the json body of the request. The **MockResponse** is the desired Response that you want to get returned by your local MockWebServer. See a simple example that defines 2 mock responses where one out of them is an error:
```Kotlin
package com.appham.mockinizer.demo

import com.appham.mockinizer.RequestFilter
import okhttp3.mockwebserver.MockResponse

val mocks: Map<RequestFilter, MockResponse> = mapOf(

    RequestFilter("/mocked") to MockResponse().apply {
        setResponseCode(200)
        setBody("""{"title": "Banana Mock"}""")
    },

    RequestFilter("/mockedError") to MockResponse().apply {
        setResponseCode(400)
    }

)
```

### 4. Add mockinizer to OkHttpClient
To wire up the mocks that you defined in step 3 you just have to call the `mockinize(mocks)` extension function in the OkHttpClient builder and provide the mocks map as the parameter, see example:
```Kotlin
OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .mockinize(mocks) // <-- just add this line
            .build()
```
Yes, that is it! Just add `.mockinize(mocks)` into the OkHttpClient.Builder chain. Happy mocking! :-)

### 5. Launch app and check logs to verify mocking is working
Once you call a mockinized api endpoint in your app then you can verify the mocked responses in the logcat. The attached HttpLogging interceptor should produce logs similar to:
```
D/OkHttp: --> GET https://my-json-server.typicode.com/typicode/demo/posts
D/OkHttp: --> END GET
D/OkHttp: --> GET https://my-json-server.typicode.com/mockedError
D/OkHttp: --> END GET
D/OkHttp: --> GET https://my-json-server.typicode.com/mocked
D/OkHttp: --> END GET
D/OkHttp: <-- 400 https://localhost:51970/mockedError (164ms)
D/OkHttp: content-length: 0
D/OkHttp: mockinizer: <-- Real request https://my-json-server.typicode.com/mockedError is now mocked to HTTP/1.1 400 Client Error
D/OkHttp: <-- END HTTP (0-byte body)
D/OkHttp: <-- 200 https://localhost:51970/mocked (175ms)
D/OkHttp: content-length: 24
D/OkHttp: mockinizer: <-- Real request https://my-json-server.typicode.com/mocked is now mocked to HTTP/1.1 200 OK
D/OkHttp: {"title": "Banana Mock"}
D/OkHttp: <-- END HTTP (24-byte body)
D/OkHttp: <-- 200 https://my-json-server.typicode.com/typicode/demo/posts (468ms)
D/OkHttp: date: Sun, 04 Aug 2019 15:43:38 GMT
D/OkHttp: content-type: application/json; charset=utf-8
D/OkHttp: set-cookie: __cfduid=d984bcb9e638f81a7717a35549ba949791564933418; expires=Mon, 03-Aug-20 15:43:38 GMT; path=/; domain=.typicode.com; HttpOnly
D/OkHttp: x-powered-by: Express
D/OkHttp: vary: Origin, Accept-Encoding
D/OkHttp: access-control-allow-credentials: true
D/OkHttp: cache-control: no-cache
D/OkHttp: pragma: no-cache
D/OkHttp: expires: -1
D/OkHttp: x-content-type-options: nosniff
D/OkHttp: etag: W/"86-YtXc+x6dfp/4aT8kTDdp4oV+9kU"
D/OkHttp: via: 1.1 vegur
D/OkHttp: expect-ct: max-age=604800, report-uri="https://report-uri.cloudflare.com/cdn-cgi/beacon/expect-ct"
D/OkHttp: server: cloudflare
D/OkHttp: cf-ray: 5011a5e80d686b65-LHR
D/OkHttp: [
D/OkHttp:   {
D/OkHttp:     "id": 1,
D/OkHttp:     "title": "Post 1"
D/OkHttp:   },
D/OkHttp:   {
D/OkHttp:     "id": 2,
D/OkHttp:     "title": "Post 2"
D/OkHttp:   },
D/OkHttp:   {
D/OkHttp:     "id": 3,
D/OkHttp:     "title": "Post 3"
D/OkHttp:   }
D/OkHttp: ]
D/OkHttp: <-- END HTTP (134-byte body)
```
In above example three api calls have been made. The only api call that wasn´t mocked is the call to https://my-json-server.typicode.com/typicode/demo/posts the other calls to `/mocked` and `/mockedError` got swapped in by Mockinizer! (Notice **localhost** responding to those with the previously defined mock responses)


## See Mockinizer Demo Project
In case you want to see how it works in action then just check out the [Mockinizer demo project](https://github.com/donfuxx/MockinizerDemo)!

## Run automated tests
A good way to see Mockinizer in action is to run the androidTests: just run `./gradlew build connectedCheck` from the terminal and see in the logcat how api calls got mocked!

## Contributing
Pull requests are welcome! Just [fork Mockinizer](https://github.com/donfuxx/Mockinizer/network/members) and add submit your PR. Also feel free to add issues for new feature requests and discovered bugs. Check the [Contributing guidelines](https://github.com/donfuxx/Mockinizer/blob/master/CONTRIBUTING.md) for more infos.

## Feedback
If you like this project then please don't forget to [stargaze Mockinizer](https://github.com/donfuxx/Mockinizer/stargazers) and share it with your friends! 

## Stay up to date
Always be the first to know about new releases and [add Mockinizer to your watches](https://github.com/donfuxx/Mockinizer/watchers).
