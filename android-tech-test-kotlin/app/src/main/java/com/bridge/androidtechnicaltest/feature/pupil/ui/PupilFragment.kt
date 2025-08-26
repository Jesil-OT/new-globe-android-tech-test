package com.bridge.androidtechnicaltest.feature.pupil.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.databinding.FragmentPupillistBinding
import com.bridge.androidtechnicaltest.feature.pupil.components.PupilAction
import com.bridge.androidtechnicaltest.feature.pupil.components.PupilRecyclerAdapter
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUI
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class PupilFragment : Fragment(R.layout.fragment_pupillist), PupilAction {
    private var _binding: FragmentPupillistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PupilViewModel by viewModel()

    private val pupilAdapter by lazy { PupilRecyclerAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPupillistBinding.bind(view)
        setUpViews()
        setUpObservers()
    }

    private fun setUpViews() = with(binding) {
        pupilList.adapter = pupilAdapter
        createNewPupilFab.setOnClickListener {
            val actions = PupilFragmentDirections.toAddPupilFragment()
            findNavController().navigate(actions)
        }
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.main_menu, menu)
                }
                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_sync -> {
                            viewModel.observerPupilChanges()
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pupilData.collect { update ->
                        pupilAdapter.submitList(update)
                        if (update.isEmpty()){
                            Toast.makeText(requireContext(),
                                getString(R.string.no_pupil_list_found), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                launch {
                    viewModel.handleEventState.collect { viewEvent ->
                        when (viewEvent) {
                            is PupilOneTimeEvent.ErrorEvent -> {
                                handleErrorEvent(event = viewEvent)
                            }
                            is PupilOneTimeEvent.LoadingEvent -> {
                                handleLoadingEvent(event = viewEvent)
                            }
                            is PupilOneTimeEvent.SuccessEvent -> {
                                handleSuccessEvent(event = viewEvent)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleErrorEvent(event: PupilOneTimeEvent.ErrorEvent) {
        hideLoading()
        Snackbar.make(binding.root, event.errorMessage, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.retry)) { viewModel.observerPupilChanges() }
            .show()
    }

    private fun handleLoadingEvent(event: PupilOneTimeEvent.LoadingEvent) = with(binding) {
        if (event.pupilList.isEmpty()) {
            shimmerLoadingView.root.visibility = View.VISIBLE
        } else {
            loadingView.visibility = View.VISIBLE
            progress.visibility = View.VISIBLE
        }
    }

    private fun handleSuccessEvent(event: PupilOneTimeEvent.SuccessEvent) {
        hideLoading()
        when (event.successSource) {
            SuccessSource.SYNCED -> {
                Snackbar.make(
                    binding.root,
                    getString(R.string.pupil_list_updated),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            SuccessSource.SINGLE_SOURCE -> {
                // No need to show message for cached data
                // it's where our data comes from
            }
        }
    }

    private fun hideLoading() = with(binding){
        shimmerLoadingView.root.visibility = View.GONE
        loadingView.visibility = View.GONE
        progress.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun navigateToPupilDetail(pupilId: Int) {
        val action = PupilFragmentDirections.actionPupilFragmentToPupilDetailFragment(pupilId)
        findNavController().navigate(action)
    }

}