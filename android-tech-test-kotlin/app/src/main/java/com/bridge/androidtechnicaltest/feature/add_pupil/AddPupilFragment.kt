package com.bridge.androidtechnicaltest.feature.add_pupil

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.databinding.FragmentAddpupilBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddPupilFragment: Fragment(R.layout.fragment_addpupil) {
    private var _binding: FragmentAddpupilBinding? =  null
    private val binding get() = _binding!!
    private val viewModel: AddPupilViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddpupilBinding.bind(view)
        binding.apply {

        }
        setUpObservers()
    }

    private fun setUpObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addPupilUiEvents.collect { event ->
                    when (event) {
                        is AddPupilUiEvents.Loading -> {}
                        is AddPupilUiEvents.PupilAddedSuccessfully -> {}
                        is AddPupilUiEvents.PupilFailedToAdd -> {}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}