package com.mewtype.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mewtype.player.App
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var cacheSize by remember { mutableStateOf("计算中...") }

    LaunchedEffect(Unit) {
        cacheSize = getCacheSize(File(App.instance.cacheDir, "media_cache"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("缓存管理", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("缓存大小: $cacheSize", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                clearCache()
                cacheSize = "0 B"
            }) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("清除缓存")
            }

            Spacer(Modifier.height(24.dp))
            Text("音视频切换", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("在播放器页面使用音视频切换按钮切换MV/纯音频模式", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(24.dp))
            Text("关于", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("MewType Player v1.0.0", style = MaterialTheme.typography.bodyMedium)
            Text("收录夢限大みゅーたいぷ全歌曲", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun getCacheSize(dir: File): String {
    if (!dir.exists()) return "0 B"
    val bytes = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

private fun clearCache() {
    val dir = File(App.instance.cacheDir, "media_cache")
    if (dir.exists()) dir.deleteRecursively()
}
