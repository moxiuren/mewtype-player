package com.mewtype.player

import android.app.Application
import com.mewtype.player.data.database.AppDatabase
import com.mewtype.player.data.network.BilibiliApi
import com.mewtype.player.data.repository.SongRepository
import com.mewtype.player.media.MewtypePlayer

class App : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var repository: SongRepository
        private set
    lateinit var api: BilibiliApi
        private set
    lateinit var player: MewtypePlayer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.get(this)
        api = BilibiliApi()
        repository = SongRepository(this, database.songDao(), api)
        player = MewtypePlayer(this)
        // Start media session service for background playback
        try {
            val intent = android.content.Intent(this, com.mewtype.player.media.PlayerService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (_: Exception) {
            // Service will start on first playback if deferred
        }
    }

    override fun onTerminate() {
        player.release()
        super.onTerminate()
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
