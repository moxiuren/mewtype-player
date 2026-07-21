package com.mewtype.player.data.network

import java.security.MessageDigest
import kotlin.random.Random

object WbiSign {
    private const val MIXIN_KEY_ENC_TABLE = "462a6480d16c219e30e2284c35ed1f64"

    private var imgKey: String = ""
    private var subKey: String = ""
    private var lastFetchTime = 0L

    private fun getMixinKey(orig: String): String {
        return MIXIN_KEY_ENC_TABLE.take(32)
            .map { c -> if (c in '0'..'9') c - '0' else c - 'a' + 10 }
            .joinToString("") { orig.getOrElse(it) { ' ' }.toString() }
            .trim()
    }

    fun encrypt(params: Map<String, String>): String {
        val mixinKey = getMixinKey(imgKey + subKey)
        val sorted = params.entries
            .filter { it.key != "wts" && it.key != "w_rid" }
            .sortedBy { it.key }
            .joinToString("&") { "${it.key}=${it.value}" } + mixinKey
        val md5 = MessageDigest.getInstance("MD5")
            .digest(sorted.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return md5
    }

    fun updateKeys(imgUrl: String, subUrl: String) {
        imgKey = imgUrl.substringAfterLast("/").substringBefore(".")
        subKey = subUrl.substringAfterLast("/").substringBefore(".")
    }

    fun needsRefresh(): Boolean = imgKey.isEmpty() || (System.currentTimeMillis() - lastFetchTime) > 3600000

    fun setLastFetchTime() { lastFetchTime = System.currentTimeMillis() }
}
