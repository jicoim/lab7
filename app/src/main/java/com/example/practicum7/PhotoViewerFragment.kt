package com.example.practicum7

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import java.io.File

private const val ARG_PHOTO_PATH = "photo_path"

class PhotoViewerFragment : DialogFragment() {

    companion object {
        fun newInstance(photoPath: String): PhotoViewerFragment {
            val args = Bundle().apply {
                putString(ARG_PHOTO_PATH, photoPath)
            }
            return PhotoViewerFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_viewer, container, false)
        val photoPath = arguments?.getString(ARG_PHOTO_PATH)

        val imageView: ImageView = view.findViewById(R.id.photo_view)

        if (photoPath != null) {
            val bitmap = getScaledBitmap(photoPath, requireActivity().window.decorView.width, requireActivity().window.decorView.height)
            imageView.setImageBitmap(bitmap)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}