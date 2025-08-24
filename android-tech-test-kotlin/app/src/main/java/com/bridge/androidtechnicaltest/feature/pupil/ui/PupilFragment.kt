package com.bridge.androidtechnicaltest.feature.pupil.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.databinding.FragmentPupillistBinding
import com.bridge.androidtechnicaltest.feature.pupil.components.PupilRecyclerAdapter
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUI
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUiState
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PupilFragment : Fragment(R.layout.fragment_pupillist) {
    private var _binding: FragmentPupillistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PupilViewModel by viewModel()

    private val pupilAdapter by lazy { PupilRecyclerAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPupillistBinding.bind(view)
        setUpViews()
        setUpObservers()
    }

    private fun setUpViews() = with(binding) {
        pupilList.adapter = pupilAdapter
    }

    private fun setUpObservers() {
        viewModel.pupilUIState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is PupilUiState.Error -> errorViewState(uiState.message)
                is PupilUiState.Loading -> loadingViewState()
                is PupilUiState.Success -> {
                    successViewState(uiState.pupils.map { it.toPupilUI() })
                    if (uiState.isStale){
                        Snackbar.make(binding.root, "Your pupil list is up to date", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun successViewState(
        pupilLists: List<PupilUI>
    ) = with(binding) {
        loadingView.root.visibility = View.GONE
        pupilList.visibility = View.VISIBLE
        pupilAdapter.submitList(pupilLists)
    }

    private fun errorViewState(errorMessage: String) = with(binding) {
        loadingView.root.visibility = View.GONE
        pupilList.visibility = View.VISIBLE
        Snackbar.make(root, errorMessage, Snackbar.LENGTH_LONG).setAction("Retry") {
            viewModel.getAllPupils()
        }.show()
    }

    private fun loadingViewState() = with(binding) {
        loadingView.root.visibility = View.VISIBLE
        pupilList.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}