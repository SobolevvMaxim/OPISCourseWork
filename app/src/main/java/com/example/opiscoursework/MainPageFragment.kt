package com.example.opiscoursework

import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.android.synthetic.main.main_page_fragment.*
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

class MainPageFragment : Fragment(R.layout.main_page_fragment) {
    companion object {
        fun create() = MainPageFragment()
    }

    private val viewModel = viewModels<MainViewModel>()

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val bitmap = MediaStore.Images.Media.getBitmap(
                requireActivity().contentResolver,
                uri
            )
            viewModel.value.postResultImage(
                bitmap
            )
            // testPython(bitmap)
        }

    private fun testPython(bitmap: Bitmap) {
        if (!Python.isStarted())
            Python.start(AndroidPlatform(requireContext()))
        val python = Python.getInstance()
        val module = python.getModule("test")
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            val image = stream.toByteArray()
            val testBytesString = image.joinToString(", ")
            Log.d("PYTHON", "Byte array: ${image.contentToString()}")
            // val bytes = String(image, 0, image.size, StandardCharsets.UTF_8)
            Log.d("PYTHON", "Bytes string: $testBytesString")
            val resultTest: String =
                module.callAttr("test", testBytesString).toJava(String::class.java)
            Log.d("PYTHON", "Test : $resultTest")
            val result: Bitmap = module.callAttr("draw", testBytesString).toJava(Bitmap::class.java)
            current_image.setImageBitmap(result)
            Toast.makeText(requireContext(), result.toString(), Toast.LENGTH_LONG).show()
            Log.d("PYTHON", "Result: $result")
        } catch (e: PyException) {
            Log.d("PYTHON", "Exception: ${e.message}")
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        choose_button.setOnClickListener {
            getContent.launch("image/*")
            progressBar.visibility = View.VISIBLE
        }

        viewModel.value.imageLiveData.observe(viewLifecycleOwner) { bitmap ->
            current_image.setImageBitmap(bitmap)
            progressBar.visibility = View.INVISIBLE
        }

        OpenCVLoader.initDebug()
    }
}