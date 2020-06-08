package top.littlefogcat.simpledatabindingkt

import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request

object HttpUtil {
    private val client: OkHttpClient =
        OkHttpClient()

    fun httpGet(url: String, callback: Callback) {
        val req = Request.Builder().url(url).build()
        client.newCall(req).enqueue(callback)
    }

    fun httpGet(url: String): String? {
        val req = Request.Builder().url(url).build()
        val response = client.newCall(req).execute()
        return response.body?.string()
    }
}