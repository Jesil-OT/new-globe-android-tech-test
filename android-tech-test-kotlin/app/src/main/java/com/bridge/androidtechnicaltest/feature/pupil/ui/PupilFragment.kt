package com.bridge.androidtechnicaltest.feature.pupil.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.databinding.FragmentPupillistBinding
import com.bridge.androidtechnicaltest.ui.PupilUiState
import org.koin.androidx.viewmodel.ext.android.viewModel

class PupilFragment: Fragment(R.layout.fragment_pupillist) {
    private var _binding: FragmentPupillistBinding? =  null
    private val binding get() = _binding!!
    private val viewModel: PupilViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPupillistBinding.bind(view)
        binding.apply {

        }
        setUpObservers()
    }

    private fun setUpObservers() {
        viewModel.pupilUIState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is PupilUiState.Error -> {}
                is PupilUiState.Loading -> {}
                is PupilUiState.Success -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}