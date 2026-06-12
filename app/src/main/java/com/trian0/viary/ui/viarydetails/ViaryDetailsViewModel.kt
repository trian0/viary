package com.trian0.viary.ui.viarydetails

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.data.repositories.toViary
import com.trian0.viary.mvi.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ViaryDetailsViewModel(
    private val repository: ViaryRepository,
) : BaseViewModel<ViaryDetailsContract.ViaryDetailsIntent, ViaryDetailsContract.ViaryDetailsUiState, ViaryDetailsContract.ViaryDetailsEffect>() {

    companion object {
        private const val TAG = "ViaryDetailsViewModel"
    }

    override fun createInitialState() = ViaryDetailsContract.ViaryDetailsUiState()

    override fun handleIntent(intent: ViaryDetailsContract.ViaryDetailsIntent) {
        when (intent) {
            is ViaryDetailsContract.ViaryDetailsIntent.Load -> loadViary(intent.viaryId)
        }
    }

    private fun loadViary(viaryId: String) {
        Log.d(TAG, "loadViary: $viaryId")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = repository.getViaryWithCheckpointsById(viaryId)
                if (result == null) {
                    setState { copy(isLoading = false) }
                    return@launch
                }
                val viary = result.toViary()
                val checkpoints = viary.checkpoints.sortedBy { it.time }

                val allPhotos = checkpoints.flatMap { cp ->
                    val photos = cp.images.toMutableList()
                    cp.imageUri?.let { photos.add(0, it) }
                    photos
                }

                val duration = formatDuration(viary.departureTime?.time, checkpoints.lastOrNull()?.time?.time)

                setState {
                    copy(
                        viary = viary,
                        checkpoints = checkpoints,
                        allPhotos = allPhotos,
                        durationFormatted = duration,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadViary error", e)
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun formatDuration(startMs: Long?, endMs: Long?): String {
        if (startMs == null || endMs == null || endMs <= startMs) return "--"
        val diff = endMs - startMs
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        return "${hours}h ${minutes}m"
    }
}
