package com.bridge.androidtechnicaltest.feature.edit_pupil

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.databinding.FragmentEditpupilBinding

class PupilEditFragment: Fragment(R.layout.fragment_editpupil) {
    private var _binding: FragmentEditpupilBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditpupilBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}