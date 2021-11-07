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

    private lateinit var src: Mat

    fun postResultImage(bitmap: Bitmap?) {
        viewModelScope.launch {
            _imageLiveData.postValue(drawContours(findContours(bitmap), bitmap))
        }
    }

    private suspend fun findContours(bitmap: Bitmap?): List<MatOfPoint> {
        return withContext(Dispatchers.IO) {
            val contours: List<MatOfPoint> = ArrayList()
            bitmap?.let {
                src = Mat()
                Utils.bitmapToMat(it, src)
                val gray = Mat()
                Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

                Imgproc.Canny(gray, gray, 10.0, 250.0) // first border + second border
                val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(2.0, 2.0)) // Size choice
                Imgproc.morphologyEx(gray, gray, Imgproc.MORPH_CLOSE, kernel)
                val hierarchy = Mat()

                Imgproc.findContours(
                    gray,
                    contours,
                    hierarchy,
                    Imgproc.RETR_EXTERNAL,
                    Imgproc.CHAIN_APPROX_SIMPLE
                )
            }
            return@withContext contours
        }
    }

    private suspend fun drawContours(contours: List<MatOfPoint>, bitmap: Bitmap?): Bitmap? {
        return withContext(Dispatchers.IO) {
            bitmap?.let {
                val hierarchy = Mat()
                for (contourIdx in contours.indices) {
                    val matOfPoint = contours[contourIdx]
                    val tempMat = MatOfPoint2f()
                    matOfPoint.convertTo(tempMat, CvType.CV_32F)

                    val sm = Imgproc.arcLength(tempMat, true)
                    val apd = MatOfPoint2f()
                    Imgproc.approxPolyDP(tempMat, apd, 0.02 * sm, true)
                    if (apd.elemSize() > 2)
                        Imgproc.drawContours(
                            src,
                            contours,
                            contourIdx,
                            Scalar(0.0, 255.0, 0.0), // Line color
                            0, // Line thickness
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