package com.mewtype.player.ui.screens

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onBack: () -> Unit
) {
    var done by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("B站登录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                "登录B站后可获得更高码率",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )

            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        @SuppressLint("SetJavaScriptEnabled")
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                if (done) return
                                // Only check cookie when navigated away from passport
                                val host = url?.let { android.net.Uri.parse(it).host } ?: return
                                if (host == "passport.bilibili.com") return
                                val cookies = CookieManager.getInstance().getCookie("https://www.bilibili.com")
                                if (cookies?.contains("SESSDATA") == true) {
                                    done = true
                                    onLoginSuccess(cookies)
                                }
                            }
                        }
                        loadUrl("https://passport.bilibili.com/login")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
