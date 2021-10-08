package com.example.opiscoursework

import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class MainViewModel : ViewModel() {
    private val _imageLiveData = MutableLiveData<Bitmap>()
    val imageLiveData: LiveData<Bitmap> get() = _imageLiveData

    fun drawContours(bitmap: Bitmap?) {
        viewModelScope.launch {
            _imageLiveData.postValue(getResult(bitmap))
        }
    }

    private suspend fun getResult(bitmap: Bitmap?): Bitmap? {
        return withContext(Dispatchers.IO) {
            bitmap?.let {
                val src = Mat()
                Utils.bitmapToMat(it, src)
                val gray = Mat()
                Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2HSV)

                Imgproc.Canny(src, gray, 0.0, 255.0)
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
                        2,
                        Imgproc.LINE_8,
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