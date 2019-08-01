package com.appham.mockinizer

import okhttp3.mockwebserver.MockResponse

val mocks: Map<RequestFilter, MockResponse> = mapOf(

        RequestFilter("/typicode/demo/mocked") to MockResponse().apply {
            setResponseCode(200)
            setBody(
                    """
                        [
                          {
                            "id": 555,
                            "title": "Banana Mock"
                          },
                          {
                            "id": 675,
                            "title": "foooo"
                          }
                        ]
                    """.trimIndent()
            )
        }

)