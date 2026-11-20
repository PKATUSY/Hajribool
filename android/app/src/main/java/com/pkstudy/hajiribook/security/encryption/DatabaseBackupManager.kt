package com.pkstudy.hajiribook.security.encryption

import android.content.Context
import com.pkstudy.hajiribook.data.local.database.HajiriDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class BackupFileInfo(
    val fileName: String,
    val filePath: String,
    val sizeBytes: Long,
    val timestamp: Long,
    val isEncrypted: Boolean = true
)

class DatabaseBackupManager(private val context: Context) {

    private val backupDir: File
        get() = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }

    private val dbFile: File
        get() = context.getDatabasePath(HajiriDatabase.DATABASE_NAME)

    /**
     * Creates an encrypted backup of the local Room database file using AES-256-GCM.
     */
    fun createEncryptedBackup(passwordKey: String = "HajiriBookSecureKey2026@IndiaSchool"): Result<BackupFileInfo> {
        return try {
            if (!dbFile.exists()) {
                return Result.failure(Exception("Database file does not exist"))
            }

            val timestamp = System.currentTimeMillis()
            val backupFileName = "hajiri_backup_$timestamp.enc"
            val outputFile = File(backupDir, backupFileName)

            // Derive 256-bit key
            val keyBytes = passwordKey.padEnd(32, '0').substring(0, 32).toByteArray(Charsets.UTF_8)
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val iv = ByteArray(12)
            SecureRandom().nextBytes(iv)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

            FileOutputStream(outputFile).use { fos ->
                // Write IV first
                fos.write(iv)
                CipherOutputStream(fos, cipher).use { cos ->
                    FileInputStream(dbFile).use { fis ->
                        fis.copyTo(cos)
                    }
                }
            }

            Result.success(
                BackupFileInfo(
                    fileName = backupFileName,
                    filePath = outputFile.absolutePath,
                    sizeBytes = outputFile.length(),
                    timestamp = timestamp
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restores the Room database from an encrypted backup file.
     */
    fun restoreFromBackup(backupFile: File, passwordKey: String = "HajiriBookSecureKey2026@IndiaSchool"): Result<Boolean> {
        return try {
            if (!backupFile.exists()) {
                return Result.failure(Exception("Backup file not found"))
            }

            val keyBytes = passwordKey.padEnd(32, '0').substring(0, 32).toByteArray(Charsets.UTF_8)
            val secretKey = SecretKeySpec(keyBytes, "AES")

            FileInputStream(backupFile).use { fis ->
                // Read 12-byte IV
                val iv = ByteArray(12)
                val read = fis.read(iv)
                if (read != 12) {
                    return Result.failure(Exception("Corrupted backup file: invalid IV"))
                }

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

                val tempRestoredFile = File(context.cacheDir, "temp_restore.db")
                CipherInputStream(fis, cipher).use { cis ->
                    FileOutputStream(tempRestoredFile).use { fos ->
                        cis.copyTo(fos)
                    }
                }

                // Replace current database file
                if (tempRestoredFile.exists() && tempRestoredFile.length() > 0) {
                    tempRestoredFile.copyTo(dbFile, overwrite = true)
                    tempRestoredFile.delete()
                    Result.success(true)
                } else {
                    Result.failure(Exception("Decryption failed or invalid backup data"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listBackups(): List<BackupFileInfo> {
        val files = backupDir.listFiles { _, name -> name.endsWith(".enc") } ?: return emptyList()
        return files.map { file ->
            BackupFileInfo(
                fileName = file.name,
                filePath = file.absolutePath,
                sizeBytes = file.length(),
                timestamp = file.lastModified()
            )
        }.sortedByDescending { it.timestamp }
    }

    fun getDatabaseSizeBytes(): Long = if (dbFile.exists()) dbFile.length() else 0L

    fun getAvailableStorageBytes(): Long = context.filesDir.freeSpace
}
