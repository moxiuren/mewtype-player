package com.mewtype.player.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val titleCn: String,
    val type: String,
    val bvid: String,
    val cid: Long,
    val album: String,
    val artist: String,
    val duration: Int,
    val cover: String,
    val isFavorite: Boolean = false
)

data class SongCatalog(
    val songs: List<SongEntry> = emptyList(),
    val edBandoriCovers: List<CoverEntry> = emptyList(),
    val bonusCovers: List<CoverEntry> = emptyList()
)

data class SongEntry(
    val id: String,
    val title: String,
    val titleEn: String = "",
    val titleCn: String,
    val type: String,
    val bvid: String,
    val bvidTV: String? = null,
    val cid: Long = 0,
    val cover: String = "",
    val releaseDate: String = "",
    val duration: Int = 0,
    val album: String = "",
    val artist: String = ""
)

data class CoverEntry(
    val id: String,
    val song: String = "",
    val title: String,
    val titleCn: String,
    val bvid: String,
    val duration: Int = 0,
    val author: String = "",
    val voiceSource: String = "",
    val plays: Long = 0,
    val likes: Long = 0
)

data class BilibiliNav(
    val wbiImg: WbiImg? = null
)

data class WbiImg(
    val imgUrl: String = "",
    val subUrl: String = ""
)

data class BilibiliVideoInfo(
    val data: VideoData? = null
)

data class VideoData(
    val bvid: String = "",
    val cid: Long = 0,
    val title: String = "",
    val pages: List<Page> = emptyList()
)

data class Page(
    val cid: Long = 0,
    val part: String = ""
)

data class BilibiliStream(
    val data: StreamData? = null
)

data class StreamData(
    val durl: List<Durl> = emptyList(),
    val dash: Dash? = null
)

data class Durl(
    val url: String = "",
    val backupUrl: List<String> = emptyList()
)

data class Dash(
    val video: List<StreamFormat> = emptyList(),
    val audio: List<StreamFormat> = emptyList()
)

data class StreamFormat(
    val id: Int = 0,
    val baseUrl: String = "",
    val backupUrl: List<String> = emptyList(),
    val mimeType: String = "",
    val codecs: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val bandwidth: Long = 0
)

data class BilibiliResponse<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T? = null
)
