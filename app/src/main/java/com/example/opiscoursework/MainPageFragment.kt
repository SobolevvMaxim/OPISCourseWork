package com.example.opiscoursework

import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.main_page_fragment.*
import org.opencv.android.OpenCVLoader

class MainPageFragment : Fragment(R.layout.main_page_fragment) {
    companion object {
        fun create() = MainPageFragment()
    }

    private val viewModel = viewModels<MainViewModel>()

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            viewModel.value.postResultImage(
                MediaStore.Images.Media.getBitmap(
                    requireActivity().contentResolver,
                    uri
                )
            )
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        choose_button.setOnClickListener {
            getContent.launch("image/*")
            progressBar.visibility = View.VISIBLE
        }

        viewModel.value.imageLiveData.observe(viewLifecycleOwner) { bitmap ->
            photo_image.setImageBitmap(bitmap)
            progressBar.visibility = View.INVISIBLE
        }

        OpenCVLoader.initDebug()
    }
}