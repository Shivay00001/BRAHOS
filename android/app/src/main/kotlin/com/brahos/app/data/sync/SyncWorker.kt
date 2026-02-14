package com.brahos.app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.brahos.app.domain.usecase.SyncTriageDataUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

/**
 * Background worker to sync triage data periodically.
 * Ensures data is pushed to backend whenever connectivity is available.
 */
class SyncWorker @Inject constructor(
    context: Context,
    workerParams: WorkerParameters,
    private val syncTriageDataUseCase: SyncTriageDataUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            syncTriageDataUseCase()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
