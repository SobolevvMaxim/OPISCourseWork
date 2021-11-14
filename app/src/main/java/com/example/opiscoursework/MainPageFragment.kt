package com.example.opiscoursework

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.main_page_fragment.*
import org.opencv.android.OpenCVLoader
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.settings_dialog.*

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
                val newCanny = CannySettings(
                    border1 = getValue(canny_border1),
                    border2 = getValue(canny_border2)
                )
                val newSize = SizeSettings(
                    border1 = getValue(size_border1),
                    border2 = getValue(size_border2)
                )
                val newLineColor = LineColor(
                    color1 = getValue(color_1),
                    color2 = getValue(color_2),
                    color3 = getValue(color_3)
                )
                val newThickness = seekbar_thickness.progress
                viewModel.value.changeSettings(Settings(newCanny, newSize, newLineColor, newThickness))
            }
            setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)) { _, _ ->
            }
            setCurrentValues(viewModel.value.getSettings())
            show()
        }
    }

    private fun setCurrentValues(settings: Settings) {
        settings.apply {
            canny.apply { 
                canny_border1.setText(border1.toString(), TextView.BufferType.EDITABLE)
                canny_border2.setText(border2.toString(), TextView.BufferType.EDITABLE)
            }

            size.apply {
                size_border1.setText(border1.toString(), TextView.BufferType.EDITABLE)
                size_border2.setText(border2.toString(), TextView.BufferType.EDITABLE)
            }

            lineColor.apply {
                color_1.setText(color1.toString(), TextView.BufferType.EDITABLE)
                color_2.setText(color2.toString(), TextView.BufferType.EDITABLE)
                color_3.setText(color3.toString(), TextView.BufferType.EDITABLE)
            }
        }
    }

    private fun getValue(ed: EditText?): Double = ed?.text.toString().toDouble()
}