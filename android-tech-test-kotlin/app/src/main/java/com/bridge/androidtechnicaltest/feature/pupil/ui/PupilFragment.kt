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
                            viewModel.getAllPupils()
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
                viewModel.pupilResponse.collect { uiState ->
                    when (uiState) {
                        is PupilResponse.Error -> {
                            errorViewState(uiState.message)
                        }

                        is PupilResponse.Loading -> loadingViewState()
                        is PupilResponse.Success -> {
                            successViewState(uiState.pupils.map { it.toPupilUI() })
                            if (uiState.isStale) {
                                Snackbar.make(
                                    binding.root,
                                    "Your pupil list has been successfully updated. You're now seeing the latest information.",
                                    Snackbar.LENGTH_LONG
                                ).show()
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

    override fun navigateToPupilDetail(pupilId: Int) {
        val action = PupilFragmentDirections.actionPupilFragmentToPupilDetailFragment(pupilId)
        findNavController().navigate(action)
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sync -> {
                viewModel.getAllPupils()
            }
        }
        return super.onContextItemSelected(item)
    }

}