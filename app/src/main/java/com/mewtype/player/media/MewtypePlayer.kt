package com.mewtype.player.media

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import java.io.File

class MewtypePlayer(context: Context) {
    private val cacheDir = File(context.cacheDir, "media_cache")
    private val simpleCache = SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(512L * 1024 * 1024)) // max 512MB
    private val upstreamFactory = DefaultHttpDataSource.Factory()
        .setDefaultRequestProperties(mapOf("Referer" to "https://www.bilibili.com"))
    private val cacheDataSourceFactory = CacheDataSource.Factory()
        .setCache(simpleCache)
        .setUpstreamDataSourceFactory(upstreamFactory)

    val trackSelector = DefaultTrackSelector(context)
    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
        .setTrackSelector(trackSelector)
        .build()

    private var isAudioOnly = false

    fun play(bvid: String, streamUrl: String, title: String, artist: String) {
        val item = MediaItem.Builder()
            .setMediaId(bvid)
            .setUri(streamUrl)
            .setMediaMetadata(MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .build())
            .build()
        player.setMediaItem(item)
        player.prepare()
        player.play()
    }

    fun toggleAudioOnly() {
        isAudioOnly = !isAudioOnly
        player.setTrackSelectionParameters(
            player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, isAudioOnly)
                .build()
        )
    }

    fun isAudioOnlyMode(): Boolean = isAudioOnly

    fun release() {
        player.release()
        simpleCache.release()
    }
}
