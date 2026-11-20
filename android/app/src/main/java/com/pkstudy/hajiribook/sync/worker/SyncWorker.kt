package com.pkstudy.hajiribook.sync.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pkstudy.hajiribook.HajiriApp

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? HajiriApp ?: return Result.failure()
        val repository = app.repository

        return try {
            val syncResult = repository.performBidirectionalSync()
            if (syncResult.isSuccess) {
                Result.success()
            } else {
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val PERIODIC_WORK_TAG = "hajiri_periodic_sync"
        const val ONE_TIME_WORK_TAG = "hajiri_onetime_sync"
    }
}
