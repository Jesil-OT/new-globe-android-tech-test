package com.bridge.androidtechnicaltest.feature.pupil_info


import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.databinding.FragmentPupildetailBinding
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUI
import com.bridge.androidtechnicaltest.feature.pupil.ui.toPupilUI
import com.bridge.androidtechnicaltest.feature.pupil_info.models.DetailPupilUiState
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class PupilDetailFragment : Fragment(R.layout.fragment_pupildetail) {
    private var _binding: FragmentPupildetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PupilDetailViewModel by viewModel()
    private val args by navArgs<PupilDetailFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPupildetailBinding.bind(view)
        viewModel.getPupil(pupilId = args.pupilId)
        setUpObservers()
        setUpViews()
    }

    private fun setUpViews() = with(binding){
        deletePupil.setOnClickListener {
            alertDialog(
                positiveButtonAction = {viewModel.deletePupil(args.pupilId)},
                negativeButtonAction = {}
            )
        }
        editPupil.setOnClickListener {
            findNavController().navigate(PupilDetailFragmentDirections.actionToPupilEditFragment())
        }
    }

    private fun setUpObservers() {
        viewModel.pupilDetailUiState.observe(viewLifecycleOwner) { details ->
            when (details) {
                is DetailPupilUiState.Error -> errorViewState(details.message){viewModel.getPupil(args.pupilId) }
                is DetailPupilUiState.Loading -> loadingViewState()
                is DetailPupilUiState.Success -> {
                    val pupil = details.pupils?.toPupilUI() ?: return@observe
                    successViewState(pupil)
                    if (details.isStale ?: return@observe){
                        Snackbar.make(binding.root, "You're now seeing their latest information.", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
        viewModel.pupilDetailDeleteState.observe(viewLifecycleOwner) { details ->
            when (details) {
                is DetailPupilUiState.Error -> errorViewState(details.message)
                is DetailPupilUiState.Loading -> loadingViewState()
                is DetailPupilUiState.Success -> {
                    Snackbar.make(binding.root, "Pupil deleted successfully", Snackbar.LENGTH_LONG).show()
                    findNavController().popBackStack()
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

    private fun errorViewState(errorMessage: String, onClick: () -> Unit = {}) = with(binding) {
        loadingView.root.visibility = View.GONE
        successView.visibility = View.VISIBLE
        Snackbar.make(root, errorMessage, Snackbar.LENGTH_LONG).setAction("Retry") {
            onClick()
        }.show()
    }

    private fun loadingViewState() = with(binding) {
        loadingView.root.visibility = View.VISIBLE
        successView.visibility = View.GONE
    }

    private fun alertDialog(
        positiveButtonAction: () -> Unit,
        negativeButtonAction: () -> Unit
    ){
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Pupil?")
            .setMessage("Are you sure you want to delete this pupil?")
            .setPositiveButton("Yes") { _, _ -> positiveButtonAction() }
            .setNegativeButton("Cancel") { _, _ -> negativeButtonAction() }
            .create()
            .show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}