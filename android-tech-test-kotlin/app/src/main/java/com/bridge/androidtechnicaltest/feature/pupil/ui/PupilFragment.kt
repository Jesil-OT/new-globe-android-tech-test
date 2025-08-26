package com.bridge.androidtechnicaltest.feature.pupil.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
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
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilResponse
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
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
                            // Handle click
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
                    viewModel.response.collect { update ->
                        when (update) {
                            is PupilUIResponse.ErrorWhileGettingSynced -> {
                                errorViewState(update.errorMessage)
                            }

                            is PupilUIResponse.Loading -> {
                                loadingViewState(
                                    shouldShowShimmer = update.pupilList.isEmpty()
                                )
                            }

                            is PupilUIResponse.ReturnedWithSyncedList -> {
                                successViewState(update.syncedList)
                            }

                            is PupilUIResponse.ReturnedFromSingleSource -> {
                                pupilAdapter.submitList(update.pupilList)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun successViewState(
        pupilLists: List<PupilUI>
    ) = with(binding) {
        pupilAdapter.submitList(pupilLists)
        shimmerLoadingView.root.visibility = View.GONE
        loadingView.visibility = View.GONE
        progress.visibility = View.GONE
        Snackbar.make(
            root,
            "Your pupil list has been successfully updated. You're now seeing the latest information.",
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun errorViewState(errorMessage: String) = with(binding) {
        shimmerLoadingView.root.visibility = View.GONE
        loadingView.visibility = View.GONE
        progress.visibility = View.GONE
        pupilList.visibility = View.VISIBLE
        Snackbar.make(root, errorMessage, Snackbar.LENGTH_LONG).setAction("Retry") {
            viewModel.observerPupilChanges()
        }.show()
    }

    private fun loadingViewState(shouldShowShimmer: Boolean) = with(binding) {
        if (shouldShowShimmer) {
            shimmerLoadingView.root.visibility = View.VISIBLE
        } else {
            loadingView.visibility = View.VISIBLE
            progress.visibility = View.VISIBLE
        }
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