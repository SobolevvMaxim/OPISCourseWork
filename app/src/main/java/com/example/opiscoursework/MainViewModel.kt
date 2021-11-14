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

    object ContoursSettings {
        var currentSettings = Settings(CannySettings(), SizeSettings(), LineColor())
    }

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

                Imgproc.Canny( // границы для нахождения углов ( значения для расчета интенсивности градиента изображения )
                    gray,
                    gray,
                    ContoursSettings.currentSettings.canny.border1,
                    ContoursSettings.currentSettings.canny.border2
                )

                val kernel = Imgproc.getStructuringElement(
                    Imgproc.MORPH_RECT, // форма контура
                    Size(               // размеры контуров
                        ContoursSettings.currentSettings.size.border1,
                        ContoursSettings.currentSettings.size.border2
                    )
                )

                Imgproc.morphologyEx(gray, gray, Imgproc.MORPH_CLOSE, kernel) // нахождение точек внутри замкнутых контуров для удаления шумов
                val hierarchy = Mat()

                Imgproc.findContours(
                    gray,
                    contours,
                    hierarchy,
                    Imgproc.RETR_TREE, // тип контуров в иерархии
                    Imgproc.CHAIN_APPROX_SIMPLE // тип сохранения точек ( либо все, либо только крайние )
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
                            Scalar(
                                ContoursSettings.currentSettings.lineColor.color1,
                                ContoursSettings.currentSettings.lineColor.color2,
                                ContoursSettings.currentSettings.lineColor.color3
                            ),
                            ContoursSettings.currentSettings.lineThickness,
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

    fun changeSettings(newSettings: Settings) {
        ContoursSettings.currentSettings = newSettings
    }

    fun getSettings(): Settings = ContoursSettings.currentSettings
}