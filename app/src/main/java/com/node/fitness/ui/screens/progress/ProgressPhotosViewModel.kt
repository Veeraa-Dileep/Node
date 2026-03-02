package com.node.fitness.ui.screens.progress

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.node.fitness.data.database.entity.ProgressPhoto
import com.node.fitness.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

data class ProgressPhotosUiState(
    val photos: List<ProgressPhoto> = emptyList(),
    val isCompareModeActive: Boolean = false,
    val comparePhoto1: ProgressPhoto? = null,
    val comparePhoto2: ProgressPhoto? = null,
    val isLoading: Boolean = true,
    val selectedPhotoForView: ProgressPhoto? = null
)

@HiltViewModel
class ProgressPhotosViewModel @Inject constructor(
    private val photoRepo: PhotoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressPhotosUiState())
    val uiState: StateFlow<ProgressPhotosUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            photoRepo.getAllPhotosChronological().collect { photos ->
                _uiState.update {
                    it.copy(photos = photos, isLoading = false)
                }
            }
        }
    }

    fun copyAndAddPhoto(uri: Uri, context: Context, notes: String = "") {
        viewModelScope.launch {
            val internalPath = withContext(Dispatchers.IO) {
                try {
                    val photosDir = File(context.filesDir, "photos")
                    if (!photosDir.exists()) photosDir.mkdirs()

                    val destFile = File(photosDir, "${UUID.randomUUID()}.jpg")

                    context.contentResolver.openInputStream(uri)?.use { input ->
                        // Decode and compress to max 1080p, 80% quality
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        BitmapFactory.decodeStream(input, null, options)

                        val maxDim = 1080
                        val scale = maxOf(1, minOf(
                            options.outWidth / maxDim,
                            options.outHeight / maxDim
                        ))

                        val decodeOptions = BitmapFactory.Options().apply {
                            inSampleSize = scale
                        }

                        // Re-open stream for actual decode
                        context.contentResolver.openInputStream(uri)?.use { decodeInput ->
                            val bitmap = BitmapFactory.decodeStream(decodeInput, null, decodeOptions)
                            if (bitmap != null) {
                                FileOutputStream(destFile).use { out ->
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                                }
                                bitmap.recycle()
                                destFile.absolutePath
                            } else null
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ProgressPhotosVM", "Failed to copy photo", e)
                    null
                }
            }

            internalPath?.let { path ->
                photoRepo.insertPhoto(
                    ProgressPhoto(filePath = path, notes = notes)
                )
            }
        }
    }

    fun addPhoto(filePath: String, notes: String = "") {
        viewModelScope.launch {
            photoRepo.insertPhoto(
                ProgressPhoto(filePath = filePath, notes = notes)
            )
        }
    }

    fun deletePhoto(photo: ProgressPhoto) {
        viewModelScope.launch {
            // Also delete file from internal storage if it exists
            withContext(Dispatchers.IO) {
                try {
                    val file = File(photo.filePath)
                    if (file.exists()) file.delete()
                } catch (_: Exception) {}
            }
            photoRepo.deletePhoto(photo)
        }
    }

    fun toggleCompareMode() {
        _uiState.update {
            it.copy(
                isCompareModeActive = !it.isCompareModeActive,
                comparePhoto1 = null,
                comparePhoto2 = null
            )
        }
    }

    fun selectComparePhoto(photo: ProgressPhoto) {
        _uiState.update { state ->
            when {
                state.comparePhoto1 == null -> state.copy(comparePhoto1 = photo)
                state.comparePhoto1?.id == photo.id -> state.copy(comparePhoto1 = null)
                state.comparePhoto2 == null -> state.copy(comparePhoto2 = photo)
                state.comparePhoto2?.id == photo.id -> state.copy(comparePhoto2 = null)
                else -> state.copy(comparePhoto1 = photo, comparePhoto2 = null)
            }
        }
    }

    fun viewPhoto(photo: ProgressPhoto) {
        _uiState.update { it.copy(selectedPhotoForView = photo) }
    }

    fun closePhotoView() {
        _uiState.update { it.copy(selectedPhotoForView = null) }
    }

    fun clearCompareSelection() {
        _uiState.update {
            it.copy(comparePhoto1 = null, comparePhoto2 = null)
        }
    }
}
