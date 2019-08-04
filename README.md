# Mockinizer
An OkHttpClient / RetroFit api call mocking library

## What is it?

Mockinizer helps Android developer to build apps with web api calls that use OkHttpClient / RetroFit by allowing them to quickly mock some web api responses with MockWebServer. Mockinizer allows to mock only specific api calls, while other api calls will still call the original server. 
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
Add the below code in the **app module's build.gradle** (Usually you want to only implement it only in debug builds and not release buils) At the time of writing the latest mockinizer_version was 0.9.3, you can get **latest release** version here: https://github.com/donfuxx/Mockinizer/releases
```gradle
dependencies {
    debugImplementation "com.github.donfuxx:Mockinizer:$mockinizer_version"
}
``` 
### 3. Define the RequestFilter / MockResponse Pairs 
Those represent each api call that you want to mock. The **RequestFilter** defines the Request Path (relative to the RetroFit Baseurl) and/or the json body of the request. The **MockResponse** is the desirec Response that you want to get returned by your local MockWebServer. See a simple example that defines 2 mock responses where one out of them is an error:
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
