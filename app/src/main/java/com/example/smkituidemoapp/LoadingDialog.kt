package com.example.smkituidemoapp // Replace with your actual package name

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.example.smkituidemoapp.databinding.LoadingDialogBinding // Import your binding class

class LoadingDialog(context: Context) : Dialog(context) {

    private var _binding: LoadingDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = LoadingDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Additional setup if needed
    }

    // Optional: override dismiss() for additional cleanup if necessary
}
