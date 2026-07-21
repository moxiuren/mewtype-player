package com.mewtype.player.data.repository

import android.content.Context
import com.google.gson.Gson
import com.mewtype.player.data.database.SongDao
import com.mewtype.player.data.model.*
import com.mewtype.player.data.network.BilibiliApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class SongRepository(
    private val context: Context,
    private val songDao: SongDao,
    private val api: BilibiliApi
) {
    // ponytail: change this URL to your remote catalog JSON
    companion object {
        private const val REMOTE_CATALOG_URL = "https://raw.githubusercontent.com/your-username/mewtype-player/main/songs.json"
    }

    private val gson = Gson()
    private val catalogClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    private var catalog: SongCatalog? = null

    suspend fun loadCatalog() {
        if (catalog != null) return
        val json = fetchRemoteCatalog() ?: readLocalCatalog()
        catalog = gson.fromJson(json, SongCatalog::class.java)
        if (catalog == null) return

        val entities = catalog!!.songs.map { s ->
            SongEntity(
                id = s.id,
                title = s.title,
                titleCn = s.titleCn,
                type = s.type,
                bvid = s.bvid,
                cid = s.cid,
                album = s.album,
                artist = s.artist,
                duration = s.duration,
                cover = s.cover
            )
        }
        songDao.insertOrIgnore(entities)
    }

    private suspend fun fetchRemoteCatalog(): String? = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url(REMOTE_CATALOG_URL).build()
            catalogClient.newCall(req).execute().body?.string()
        } catch (_: Exception) { null }
    }

    private fun readLocalCatalog(): String {
        return context.assets.open("songs.json")
            .bufferedReader().use { it.readText() }
    }

    fun getAllSongs(): Flow<List<SongEntity>> = songDao.getAll()
    fun getFavorites(): Flow<List<SongEntity>> = songDao.getFavorites()
    fun getEdCovers(): List<CoverEntry> = catalog?.edBandoriCovers ?: emptyList()
    fun getBonusCovers(): List<CoverEntry> = catalog?.bonusCovers ?: emptyList()

    suspend fun getStreamUrl(bvid: String, cid: Long): Result<BilibiliStream> {
        return api.fetchStream(bvid, cid)
    }

    suspend fun getVideoInfo(bvid: String): BilibiliVideoInfo? {
        return api.fetchVideoInfo(bvid)
    }

    fun setCookie(cookie: String) {
        api.setCookie(cookie)
    }

    suspend fun toggleFavorite(id: String) = songDao.toggleFavorite(id)
}
