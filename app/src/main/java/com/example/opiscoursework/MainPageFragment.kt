package com.example.opiscoursework

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.main_page_fragment.*
import org.opencv.android.OpenCVLoader
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.settings_dialog.*
import kotlin.math.roundToInt

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
                    border1 = getCurrentValue(canny_border1),
                    border2 = getCurrentValue(canny_border2)
                )
                val newSize = ContourThickness(
                    border1 = getCurrentValue(size_border1),
                    border2 = getCurrentValue(size_border1)
                )
                val newLineColor = DrawLineColor(
                    color1 = getCurrentValue(color_1),
                    color2 = getCurrentValue(color_2),
                    color3 = getCurrentValue(color_3)
                )
                val newThickness = seekbar_thickness.progress
                viewModel.value.changeSettings(
                    Settings(
                        newCanny,
                        newSize,
                        newLineColor,
                        newThickness
                    )
                )
            }
            setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)) { _, _ ->
            }

            show()

            val settings = viewModel.value.getSettings()
            settings.apply {
                canny.apply {
                    canny_border1.setCurrentValue(canny.border1)
                    canny_border2.setCurrentValue(canny.border2)
                }

                size.apply {
                    size_border1.setText(border1.toString(), TextView.BufferType.EDITABLE)
                }

                drawLineColor.apply {
                    color_1.setCurrentValue(color1)
                    color_2.setCurrentValue(color2)
                    color_3.setCurrentValue(color3)
                }

                seekbar_thickness.progress = drawLineThickness
            }
        }
    }

    private fun getCurrentValue(ed: EditText?): Double = ed?.text.toString().toDouble()

    private fun EditText.setCurrentValue(value: Double) {
        this.setText(value.roundToInt().toString(), TextView.BufferType.EDITABLE)
    }
}