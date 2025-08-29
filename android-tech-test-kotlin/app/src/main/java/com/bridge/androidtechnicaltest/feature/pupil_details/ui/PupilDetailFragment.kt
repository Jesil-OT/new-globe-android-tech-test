package com.bridge.androidtechnicaltest.feature.pupil_details.ui


import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.core.utils.ui.provideGlide
import com.bridge.androidtechnicaltest.databinding.FragmentPupildetailBinding
import com.bridge.androidtechnicaltest.feature.pupil_details.models.DetailPupilUI
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PupilDetailFragment : Fragment(R.layout.fragment_pupildetail) {
    private var _binding: FragmentPupildetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PupilDetailViewModel by viewModel()
    private val args by navArgs<PupilDetailFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPupildetailBinding.bind(view)
        setUpObservers()
        setUpViews()
    }

    private fun setUpViews() = with(binding) {
        deletePupil.setOnClickListener {
            alertDialog(
                positiveButtonAction = {
                    viewModel.deletePupil(args.pupilId)
                },
                negativeButtonAction = {}
            )
        }
        swipeRefresh.setOnRefreshListener {
            viewModel.observerPupilChanges(
                pupilId = args.pupilId
            )
        }
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.details_menu, menu)
                }

                override fun onPrepareMenu(menu: Menu) {
                    val editItem = menu.findItem(R.id.edit_pupil)
                    editItem?.isEnabled = viewModel.isPupilFound.value
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.edit_pupil -> {
                            // Handle click
                            findNavController().navigate(
                                PupilDetailFragmentDirections.actionToPupilEditFragment(args.pupilId)
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
        viewModel.observerPupilChanges(
            pupilId =
            args.pupilId
        )
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pupilDetails.collect { pupil ->
                        updateViews(pupil)
                        hideLoading()
                    }
                }
                // for observing the menu state
                launch {
                    viewModel.isPupilFound.collect { found ->
                        requireActivity().invalidateOptionsMenu()
                    }
                }
                launch {
                    viewModel.handleEventState.collect { viewEvent ->
                        when (viewEvent) {

                            is PupilDetailOneTimeEvent.ErrorEvent -> handleErrorEvent(viewEvent.errorMessage)

                            is PupilDetailOneTimeEvent.LoadingEvent -> handleLoadingEvent()

                            is PupilDetailOneTimeEvent.SuccessEvent -> handleSuccessEvent()

                            is PupilDetailOneTimeEvent.NotFoundEvent -> showNotFoundMessage()

                        }
                    }
                }
                launch {
                    viewModel.deleteEventState.collect { deleteEvent ->
                        when (deleteEvent) {
                            is DeleteOneTimeEvent.LoadingEvent -> handleLoadingEvent()

                            is DeleteOneTimeEvent.SuccessEvent -> {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.pupil_deleted_successfully),
                                    Toast.LENGTH_SHORT
                                ).show()
                                delay(500) // Short delay for UI to update
                                findNavController().popBackStack()
                            }

                            is DeleteOneTimeEvent.DeleteErrorEvent -> handleDeleteErrorEvent(
                                deleteEvent.errorMessage
                            )

                            is DeleteOneTimeEvent.NotFoundEvent -> showNotFoundMessage()
                        }
                    }
                }
            }
        }
    }

    private fun handleSuccessEvent() {
        hideLoading()
    }

    private fun handleDeleteErrorEvent(
        eventMessage: Int
    ) {
        hideLoading()
        Snackbar.make(
            binding.root,
            eventMessage,
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun handleErrorEvent(
        errorMessage: Int
    ) {
        hideLoading()
        Snackbar.make(
            binding.root,
            errorMessage,
            Snackbar.LENGTH_LONG
        ).show()
        updateViews(
            DetailPupilUI(
                pupilId = "No pupil ID found",
                pupilName = "No pupil name found",
                pupilCountry = "No pupil country found",
            )
        )
    }

    private fun handleLoadingEvent() = with(binding) {
        swipeRefresh.isRefreshing = true
        deletePupil.isEnabled = false
    }

    private fun updateViews(pupil: DetailPupilUI) = with(binding) {
        pupilName.text = pupil.pupilName
        pupilCountry.text = pupil.pupilCountry
        pupilId.text = pupil.pupilId
        root.provideGlide(pupilImage, pupil.pupilImage)
    }

    private fun showNotFoundMessage() = with(binding) {
        deletePupil.isEnabled = false
        notFoundError.visibility = View.VISIBLE
        bodyGroup.visibility = View.GONE
        swipeRefresh.isRefreshing = false
    }

    private fun hideLoading() = with(binding) {
        deletePupil.isEnabled = true
        swipeRefresh.isRefreshing = false
    }

    private fun alertDialog(
        positiveButtonAction: () -> Unit,
        negativeButtonAction: () -> Unit
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_pupil))
            .setMessage(getString(R.string.delete_dialog_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> positiveButtonAction() }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> negativeButtonAction() }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}