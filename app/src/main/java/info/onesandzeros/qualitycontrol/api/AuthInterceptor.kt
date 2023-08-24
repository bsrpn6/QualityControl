package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val urlWithCode = originalRequest.url.newBuilder()
            .addQueryParameter("code", "WK6RkqB7hXx_PQMLZFe5uBOo6gYhOj9AtzSJ3getnRQqAzFu0zk4Dw==")
            .build()

        val requestWithCode = originalRequest.newBuilder()
            .url(urlWithCode)
            .build()

        return chain.proceed(requestWithCode)
    }
}
