package com.mewtype.player.data.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mewtype.player.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException

class BilibiliApi(private val client: okhttp3.OkHttpClient = NetworkModule.okHttpClient) {

    private val gson = Gson()
    private var cookie = ""

    fun setCookie(c: String) { cookie = c }

    suspend fun fetchNav(): BilibiliNav? = withContext(Dispatchers.IO) {
        val body = httpGet("https://api.bilibili.com/x/web-interface/nav") ?: return@withContext null
        try { gson.fromJson(body, BilibiliNav::class.java) } catch (e: Exception) { null }
    }

    suspend fun fetchVideoInfo(bvid: String): BilibiliVideoInfo? = withContext(Dispatchers.IO) {
        val body = httpGet("https://api.bilibili.com/x/web-interface/view?bvid=$bvid") ?: return@withContext null
        try { gson.fromJson(body, BilibiliVideoInfo::class.java) } catch (e: Exception) { null }
    }

    suspend fun fetchStream(bvid: String, cid: Long): Result<BilibiliStream> = withContext(Dispatchers.IO) {
        try {
            if (WbiSign.needsRefresh()) {
                fetchNav()?.wbiImg?.let {
                    WbiSign.updateKeys(it.imgUrl, it.subUrl)
                    WbiSign.setLastFetchTime()
                }
            }
            val params = mutableMapOf("bvid" to bvid, "cid" to cid.toString())
            params["wts"] = (System.currentTimeMillis() / 1000).toString()
            params["w_rid"] = WbiSign.encrypt(params)
            val url = "https://api.bilibili.com/x/player/wbi/v2?" +
                    params.entries.joinToString("&") { "${it.key}=${it.value}" }

            val body = httpGet(url) ?: return@withContext fallbackStream(bvid, cid)
            val resp: BilibiliResponse<StreamData>? = try {
                gson.fromJson(body, object : TypeToken<BilibiliResponse<StreamData>>() {}.type)
            } catch (e: Exception) { null }
            if (resp != null && resp.code == 0 && resp.data != null) {
                Result.success(BilibiliStream(resp.data))
            } else fallbackStream(bvid, cid)
        } catch (e: Exception) { fallbackStream(bvid, cid) }
    }

    private suspend fun fallbackStream(bvid: String, cid: Long): Result<BilibiliStream> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.bilibili.com/x/player/playurl?bvid=$bvid&cid=$cid&qn=80&platform=html5"
            val body = httpGet(url) ?: return@withContext Result.failure(IOException("Empty response"))
            val resp: BilibiliResponse<StreamData>? = try {
                gson.fromJson(body, object : TypeToken<BilibiliResponse<StreamData>>() {}.type)
            } catch (e: Exception) { null }
            if (resp != null && resp.code == 0 && resp.data != null) {
                Result.success(BilibiliStream(resp.data))
            } else Result.failure(IOException("Stream unavailable: ${resp?.message}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun httpGet(url: String): String? {
        try {
            val req = Request.Builder().url(url)
            if (cookie.isNotEmpty()) req.header("Cookie", cookie)
            return client.newCall(req.build()).execute().body?.string()
        } catch (e: Exception) { return null }
    }
}
