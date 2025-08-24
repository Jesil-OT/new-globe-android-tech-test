package com.bridge.androidtechnicaltest.feature.pupil_info


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.databinding.FragmentPupildetailBinding
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUI
import com.bridge.androidtechnicaltest.feature.pupil.ui.toPupilUI
import com.bridge.androidtechnicaltest.feature.pupil_info.models.DetailPupilUiState
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class PupilDetailFragment : Fragment(R.layout.fragment_pupildetail) {
    private var _binding: FragmentPupildetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PupilDetailViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPupildetailBinding.bind(view)
        viewModel.getPupil(pupilId = 0)
        setUpObservers()
    }

    private fun setUpObservers() {
        viewModel.pupilDetailUiState.observe(viewLifecycleOwner) { details ->
            when (details) {
                is DetailPupilUiState.Error -> errorViewState(pupilId = 0, details.message)
                is DetailPupilUiState.Loading -> loadingViewState()
                is DetailPupilUiState.Success -> {
                    successViewState(details.pupils.toPupilUI())
                    if (details.isStale){
                        Snackbar.make(binding.root, "Your pupil list is up to date", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun successViewState(pupil: PupilUI) = with(binding) {
        loadingView.root.visibility = View.GONE
        successView.visibility = View.VISIBLE
        pupilName.text = pupil.pupilName
        pupilCountry.text = pupil.pupilCountry
        pupilId.text = pupil.pupilId
        Glide.with(binding.root)
            .load(pupil.pupilImage)
            .into(pupilImage)
    }

    private fun errorViewState(pupilId: Int, errorMessage: String) = with(binding) {
        loadingView.root.visibility = View.GONE
        successView.visibility = View.VISIBLE
        Snackbar.make(root, errorMessage, Snackbar.LENGTH_LONG).setAction("Retry") {
            viewModel.getPupil(pupilId)
        }.show()
    }

    private fun loadingViewState() = with(binding) {
        loadingView.root.visibility = View.VISIBLE
        successView.visibility = View.GONE
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}