package com.example.opiscoursework

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class MainViewModel : ViewModel() {
    private val _imageLiveData = MutableLiveData<Bitmap>()
    val imageLiveData: LiveData<Bitmap> get() = _imageLiveData

    fun postResultImage(bitmap: Bitmap?) {
        viewModelScope.launch {
            _imageLiveData.postValue(drawContours(bitmap))
        }
    }

    private suspend fun drawContours(bitmap: Bitmap?): Bitmap? {
        return withContext(Dispatchers.IO) {
            bitmap?.let {
                val src = Mat()
                Utils.bitmapToMat(it, src)
                val gray = Mat()
                Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

//                Core.inRange(src, Scalar(0.0, 0.0, 0.0), Scalar(0.0, 0.0, 0.0), gray)
                Imgproc.blur(src, gray, Size(3.0, 3.0))
                Imgproc.Canny(src, gray, 50.0, 150.0, 3, false)
                val contours: List<MatOfPoint> = ArrayList()
                val hierarchy = Mat()
                Imgproc.findContours(
                    gray,
                    contours,
                    hierarchy,
                    Imgproc.RETR_TREE,
                    Imgproc.CHAIN_APPROX_SIMPLE
                )
                for (contourIdx in contours.indices) {
                    Imgproc.drawContours(
                        src,
                        contours,
                        contourIdx,
                        Scalar(255.0),
                        1,
                        Imgproc.LINE_AA,
                        hierarchy,
                        0
                    )
                }
                val result = Bitmap.createBitmap(it.width, it.height, it.config)
                Utils.matToBitmap(src, result)
                return@withContext result
            }
        }
    }
}