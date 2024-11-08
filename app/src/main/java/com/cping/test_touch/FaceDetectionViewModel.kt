package com.cping.test_touch

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FaceDetectionViewModel : ViewModel() {
    private val _faceDetected = MutableStateFlow<List<FaceDetectionData>>(emptyList())
    val faceDetected: StateFlow<List<FaceDetectionData>> = _faceDetected.asStateFlow()

    fun onFaceDetected(faces: List<FaceDetectionData>) {
        _faceDetected.value = faces
    }

    companion object {
        private var instance: FaceDetectionViewModel? = null

        fun getInstance(): FaceDetectionViewModel {
            return instance ?: synchronized(this) {
                instance ?: FaceDetectionViewModel().also { instance = it }
            }
        }
    }
}