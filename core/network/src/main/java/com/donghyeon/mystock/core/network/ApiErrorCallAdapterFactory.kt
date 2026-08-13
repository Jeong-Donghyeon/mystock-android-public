package com.donghyeon.mystock.core.network

import com.donghyeon.mystock.core.model.ApiException
import com.donghyeon.mystock.core.network.dto.ErrorDto
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

private const val UNPARSEABLE_ERROR_MESSAGE = "서버 응답을 처리하지 못했습니다"

/** 2xx가 아닌 응답을 Retrofit의 HttpException 대신 계약 오류 본문을 담은 [ApiException]으로 바꾼다. */
internal class ApiErrorCallAdapterFactory(private val json: Json) : CallAdapter.Factory() {

    override fun get(returnType: Type, annotations: Array<out Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) return null
        val responseType = getParameterUpperBound(0, returnType as ParameterizedType)
        return object : CallAdapter<Any, Call<Any>> {
            override fun responseType(): Type = responseType

            override fun adapt(call: Call<Any>): Call<Any> = ApiErrorCall(call, json)
        }
    }
}

private class ApiErrorCall<T>(private val delegate: Call<T>, private val json: Json) : Call<T> by delegate {

    override fun enqueue(callback: Callback<T>) = delegate.enqueue(
        object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    callback.onResponse(this@ApiErrorCall, response)
                } else {
                    callback.onFailure(this@ApiErrorCall, response.toApiException(json))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) = callback.onFailure(this@ApiErrorCall, t)
        },
    )

    override fun execute(): Response<T> {
        val response = delegate.execute()
        if (!response.isSuccessful) throw response.toApiException(json)
        return response
    }

    override fun clone(): Call<T> = ApiErrorCall(delegate.clone(), json)
}

// 서버 앞의 프록시나 크래시는 계약 형태가 아닌 본문을 내므로 상태 코드만이라도 남긴다
private fun Response<*>.toApiException(json: Json): ApiException {
    val error = runCatching { json.decodeFromString<ErrorDto>(errorBody()?.string().orEmpty()) }.getOrNull()
    return ApiException(
        code = error?.code ?: "HTTP_${code()}",
        message = error?.message ?: UNPARSEABLE_ERROR_MESSAGE,
    )
}
