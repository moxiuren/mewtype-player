package com.mewtype.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mewtype.player.data.model.CoverEntry
import com.mewtype.player.data.model.SongEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(
    state: MainUiState,
    onPlaySong: (SongEntity) -> Unit,
    onPlayCover: (CoverEntry) -> Unit,
    onTabChange: (Int) -> Unit,
    onSettings: () -> Unit,
    onLogin: () -> Unit,
    onNavigateToPlayer: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MewType Player") },
                actions = {
                    if (!state.isLoggedIn) {
                        IconButton(onClick = onLogin) {
                            Icon(Icons.Default.AccountCircle, "登录")
                        }
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, "设置")
                    }
                }
            )
        },
        bottomBar = {
            state.currentSong?.let { nowPlaying ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPlayer() }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(nowPlaying.titleCn, style = MaterialTheme.typography.titleSmall)
                            Text(nowPlaying.artist, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onPlaySong(nowPlaying) }) {
                            Icon(
                                if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (state.isPlaying) "暂停" else "播放"
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = state.tab) {
                Tab(selected = state.tab == 0, onClick = { onTabChange(0) },
                    text = { Text("原创 (${state.songs.size})") })
                Tab(selected = state.tab == 1, onClick = { onTabChange(1) },
                    text = { Text("翻唱 (${state.covers.size})") })
                Tab(selected = state.tab == 2, onClick = { onTabChange(2) },
                    text = { Text("ED翻唱 (${state.edBandoriCovers.size})") })
                Tab(selected = state.tab == 3, onClick = { onTabChange(3) },
                    text = { Text("特别 (${state.bonusCovers.size})") })
            }

            when (state.tab) {
                0 -> SongList(state.songs, onPlaySong)
                1 -> SongList(state.covers, onPlaySong)
                2 -> CoverList(state.edBandoriCovers, onPlayCover)
                3 -> CoverList(state.bonusCovers, onPlayCover)
            }
        }
    }
}

@Composable
private fun SongList(songs: List<SongEntity>, onPlay: (SongEntity) -> Unit) {
    if (songs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无歌曲")
        }
        return
    }
    LazyColumn {
        items(songs) { song ->
            ListItem(
                headlineContent = {
                    Text(song.titleCn, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                supportingContent = {
                    Text(song.artist, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                trailingContent = {
                    Text("${song.duration / 60}:${String.format("%02d", song.duration % 60)}")
                },
                modifier = Modifier.clickable { onPlay(song) }
            )
        }
    }
}

@Composable
private fun CoverList(covers: List<CoverEntry>, onPlay: (CoverEntry) -> Unit) {
    if (covers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无翻唱")
        }
        return
    }
    LazyColumn {
        items(covers) { cover ->
            ListItem(
                headlineContent = {
                    Text(cover.titleCn, maxLines = 2, overflow = TextOverflow.Ellipsis)
                },
                supportingContent = {
                    Text("${cover.voiceSource} · ${cover.author}", maxLines = 1)
                },
                trailingContent = {
                    if (cover.plays > 0) {
                        Text(formatCount(cover.plays), style = MaterialTheme.typography.bodySmall)
                    }
                },
                modifier = Modifier.clickable { onPlay(cover) }
            )
        }
    }
}

private fun formatCount(count: Long): String = when {
    count >= 10000 -> "${count / 10000}万"
    count >= 1000 -> "${count / 1000}千"
    else -> count.toString()
}
