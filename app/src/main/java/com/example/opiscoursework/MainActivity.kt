package com.example.opiscoursework

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.imgproc.Imgproc
import org.opencv.core.Scalar
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var image: ImageView
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            setImageOnScreen(uri)
            drawContours1(MediaStore.Images.Media.getBitmap(this.contentResolver, uri))
        }

    private fun drawContours1(bitmap: Bitmap?) {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)
        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLORMAP_PINK)

        Imgproc.Canny(gray, gray, 50.0, 200.0)
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
            Imgproc.drawContours(src, contours, contourIdx, Scalar(0.0, 0.0, 255.0), 1)
        }
        val result = bitmap?.config?.let { Bitmap.createBitmap(bitmap.width, bitmap.height, it) }
        Utils.matToBitmap(src, result)
        image.setImageBitmap(result)
    }

    private fun setImageOnScreen(uri: Uri?) {
        image.apply {
            setImageURI(null)
            setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.choose_button).setOnClickListener {
            openGalleryForImage()
        }

        OpenCVLoader.initDebug()

        image = findViewById(R.id.current_image)
    }

    private fun openGalleryForImage() {
        getContent.launch("image/*")
    }
}