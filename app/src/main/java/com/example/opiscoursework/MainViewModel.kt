package com.example.opiscoursework

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class MainViewModel: ViewModel() {
    private val _imageLiveData =  MutableLiveData<Bitmap>()
    val imageLiveData:LiveData<Bitmap> get() = _imageLiveData

    fun drawContours(bitmap: Bitmap?) {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)
        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLORMAP_PINK)

        Imgproc.Canny(gray, gray, 10.0, 50.0)
        val contours: List<MatOfPoint> = ArrayList()
        val hierarchy = Mat()
        Imgproc.findContours(
            gray,
            contours,
            hierarchy,
            Imgproc.RETR_LIST,
            Imgproc.CHAIN_APPROX_NONE
        )
        for (contourIdx in contours.indices) {
            Imgproc.drawContours(src, contours, contourIdx, Scalar(255.0, 0.0, 0.0), 0)
        }
        val result = bitmap?.config?.let { Bitmap.createBitmap(bitmap.width, bitmap.height, it) }
        Utils.matToBitmap(src, result)
        _imageLiveData.postValue(result!!)
    }
}