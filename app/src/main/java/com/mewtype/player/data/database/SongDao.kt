package com.mewtype.player.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mewtype.player.data.model.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY id ASC")
    fun getAll(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE type = :type ORDER BY id ASC")
    fun getByType(type: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getById(id: String): SongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(songs: List<SongEntity>)

    @Query("UPDATE songs SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String)

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY id ASC")
    fun getFavorites(): Flow<List<SongEntity>>

    @Query("DELETE FROM songs")
    suspend fun clearAll()
}
