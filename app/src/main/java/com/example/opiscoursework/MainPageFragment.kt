package com.example.opiscoursework

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.main_page_fragment.*
import org.opencv.android.OpenCVLoader
import androidx.appcompat.app.AlertDialog

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

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                showSettingsDialog()
                Toast.makeText(context, "Settings pressed", Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("InflateParams")
    private fun showSettingsDialog() {
        AlertDialog.Builder(requireContext()).create().apply {
            val inflater = requireActivity().layoutInflater
            setView(inflater.inflate(R.layout.settings_dialog, null))
            setTitle(R.string.settings)
            setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok)) { _, _ ->

            }
            setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)) { _, _ ->

            }
            show()
        }
    }
}