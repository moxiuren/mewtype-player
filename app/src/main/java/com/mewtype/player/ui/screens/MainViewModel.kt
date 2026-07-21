package com.mewtype.player.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mewtype.player.App
import com.mewtype.player.data.model.CoverEntry
import com.mewtype.player.data.model.SongEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val songs: List<SongEntity> = emptyList(),
    val covers: List<SongEntity> = emptyList(),
    val edBandoriCovers: List<CoverEntry> = emptyList(),
    val bonusCovers: List<CoverEntry> = emptyList(),
    val isLoading: Boolean = true,
    val currentSong: SongEntity? = null,
    val isPlaying: Boolean = false,
    val isAudioOnly: Boolean = false,
    val isLoggedIn: Boolean = false,
    val tab: Int = 0,
    val isResolvingStream: Boolean = false,
    val error: String? = null
)

private const val TAG = "MainViewModel"

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as App
    private val repo = app.repository

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.loadCatalog()
            repo.getAllSongs().collect { all ->
                _state.update {
                    it.copy(
                        songs = all.filter { s -> s.type == "original" },
                        covers = all.filter { s -> s.type == "cover" },
                        edBandoriCovers = repo.getEdCovers(),
                        bonusCovers = repo.getBonusCovers(),
                        isLoading = false
                    )
                }
            }
        }
        viewModelScope.launch {
            app.player.player.addListener(object : androidx.media3.common.Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _state.update { it.copy(isPlaying = isPlaying) }
                }
            })
        }
    }

    fun selectTab(tab: Int) { _state.update { it.copy(tab = tab) } }

    fun playSong(song: SongEntity) {
        _state.update { it.copy(currentSong = song, isAudioOnly = app.player.isAudioOnlyMode()) }
        resolveAndPlay(song.bvid, song.cid, song.titleCn, song.artist)
    }

    fun playCover(cover: CoverEntry) {
        _state.update {
            it.copy(
                currentSong = SongEntity(
                    id = cover.id,
                    title = cover.title,
                    titleCn = cover.titleCn,
                    type = "cover",
                    bvid = cover.bvid,
                    cid = 0,
                    album = "",
                    artist = cover.author,
                    duration = cover.duration,
                    cover = ""
                ),
                isAudioOnly = app.player.isAudioOnlyMode()
            )
        }
        viewModelScope.launch {
            _state.update { it.copy(isResolvingStream = true, error = null) }
            val cid = fetchCid(cover.bvid)
            if (cid == null) {
                _state.update { it.copy(isResolvingStream = false, error = "无法获取视频信息") }
                return@launch
            }
            resolveAndPlay(cover.bvid, cid, cover.titleCn, cover.author)
        }
    }

    private fun resolveAndPlay(bvid: String, cid: Long, title: String, artist: String) {
        viewModelScope.launch {
            _state.update { it.copy(isResolvingStream = true, error = null) }
            val result = repo.getStreamUrl(bvid, cid)
            val stream = result.getOrNull()
            if (stream == null) {
                Log.e(TAG, "Failed to resolve stream for $bvid", result.exceptionOrNull())
                _state.update { it.copy(isResolvingStream = false, error = "解析流地址失败") }
                return@launch
            }
            val url = pickStreamUrl(stream)
            if (url == null) {
                _state.update { it.copy(isResolvingStream = false, error = "未找到可用的流") }
                return@launch
            }
            app.player.play(bvid, url, title, artist)
            _state.update { it.copy(isResolvingStream = false) }
        }
    }

    private fun pickStreamUrl(stream: com.mewtype.player.data.model.BilibiliStream): String? {
        val data = stream.data ?: return null
        val dash = data.dash
        if (dash != null && dash.audio.isNotEmpty()) {
            return dash.audio.first().baseUrl.ifEmpty { null }
                ?: dash.audio.first().backupUrl.firstOrNull()
        }
        if (data.durl.isNotEmpty()) {
            return data.durl.first().url.ifEmpty { null }
                ?: data.durl.first().backupUrl.firstOrNull()
        }
        return null
    }

    private suspend fun fetchCid(bvid: String): Long? {
        val info = repo.getVideoInfo(bvid) ?: return null
        val cid = info.data?.cid ?: 0
        if (cid > 0) return cid
        return info.data?.pages?.firstOrNull()?.cid
    }

    fun togglePlayPause() {
        app.player.player.playWhenReady = !app.player.player.playWhenReady
    }

    fun toggleAudioOnly() {
        app.player.toggleAudioOnly()
        _state.update { it.copy(isAudioOnly = app.player.isAudioOnlyMode()) }
    }

    fun setCookie(cookie: String) {
        repo.setCookie(cookie)
        _state.update { it.copy(isLoggedIn = true) }
    }

    fun toggleFavorite(songId: String) {
        viewModelScope.launch { repo.toggleFavorite(songId) }
    }
}
