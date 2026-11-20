package com.pkstudy.hajiribook.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pkstudy.hajiribook.data.local.entity.AppSettingEntity
import com.pkstudy.hajiribook.data.local.entity.NotificationEntity
import com.pkstudy.hajiribook.data.local.entity.SyncLogEntity
import com.pkstudy.hajiribook.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingQueue(): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'FAILED'")
    fun getFailedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueEntity): Long

    @Query("UPDATE sync_queue SET status = :status, retryCount = retryCount + 1, lastError = :error WHERE id = :id")
    suspend fun updateQueueStatus(id: Long, status: String, error: String? = null)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun removeQueueItem(id: Long)

    @Insert
    suspend fun insertLog(log: SyncLogEntity)

    @Query("SELECT * FROM sync_logs ORDER BY timestamp DESC LIMIT 20")
    fun getRecentLogs(): Flow<List<SyncLogEntity>>

    @Query("SELECT * FROM sync_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLastSyncLog(): Flow<SyncLogEntity?>
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}

@Dao
interface AppSettingsDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    fun observeSetting(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)
}
