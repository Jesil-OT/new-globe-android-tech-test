package com.bridge.androidtechnicaltest.feature.pupil_info


import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.databinding.FragmentPupildetailBinding
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUI
import com.bridge.androidtechnicaltest.feature.pupil.ui.toPupilUI
import com.bridge.androidtechnicaltest.feature.pupil_info.models.DetailPupilUiState
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
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

    private fun setUpViews() = with(binding) {
        deletePupil.setOnClickListener {
            alertDialog(
                positiveButtonAction = { viewModel.deletePupil(args.pupilId) },
                negativeButtonAction = {}
            )
        }
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.details_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.edit_pupil -> {
                            // Handle click
                            findNavController().navigate(
                                PupilDetailFragmentDirections.actionToPupilEditFragment(
                                    args.pupilId
                                )
                            )
                            true
                        }

                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    private fun setUpObservers() {
        viewModel.pupilDetailUiState.observe(viewLifecycleOwner) { details ->
            when (details) {
                is DetailPupilUiState.Error -> errorViewState(details.message) {
                    viewModel.getPupil(
                        args.pupilId
                    )
                }

                is DetailPupilUiState.Loading -> loadingViewState()
                is DetailPupilUiState.Success -> {
                    val pupil = details.pupils?.toPupilUI() ?: return@observe
                    successViewState(pupil)
                    if (details.isStale ?: return@observe) {
                        Snackbar.make(
                            binding.root,
                            "You're now seeing their latest information.",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        viewModel.pupilDetailDeleteState.observe(viewLifecycleOwner) { details ->
            when (details) {
                is DetailPupilUiState.Error -> errorViewState(details.message)
                is DetailPupilUiState.Loading -> loadingViewState()
                is DetailPupilUiState.Success -> {
                    Snackbar.make(binding.root, "Pupil deleted successfully", Snackbar.LENGTH_LONG)
                        .show()
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

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_sync)
            .error(R.drawable.ic_error)
        Glide.with(binding.root)
            .load(pupil.pupilImage)
            .apply(requestOptions)
            .transition(DrawableTransitionOptions.withCrossFade())
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
    ) {
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