package com.bridge.androidtechnicaltest.feature.pupil.ui

import android.os.Bundle
import android.util.Log
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

const val TAG = "PupilFragment"

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
        swipeRefresh.setOnRefreshListener {
            viewModel.observerPupilsChanges()
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
                            viewModel.observerPupilsChanges()
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
        viewModel.observerPupilsChanges()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pupilsData.collect { update ->
                        pupilAdapter.submitList(update)
                        hideLoading()
                    }
                }
                launch {
                    viewModel.emptyListEvent.collect { isEmpty ->
                        if (isEmpty) showNotFoundMessage()
                    }
                }
                launch {
                    viewModel.handleEventState.collect { viewEvent ->
                        when (viewEvent) {
                            is PupilOneTimeEvent.ErrorEvent -> handleErrorEvent(viewEvent.errorMessage)

                            is PupilOneTimeEvent.LoadingEvent -> handleLoadingEvent()

                            is PupilOneTimeEvent.SuccessEvent -> handleSuccessEvent()

                            is PupilOneTimeEvent.NotFoundEvent -> showNotFoundMessage()
                        }
                    }
                }
            }
        }
    }

    private fun handleErrorEvent(eventMessage: Int)= with(binding) {
        hideLoading()
        pupilList.isEnabled = true
        Snackbar.make(root, eventMessage, Snackbar.LENGTH_LONG).show()
    }

    private fun handleLoadingEvent() = with(binding) {
        pupilList.isEnabled = false
        swipeRefresh.isRefreshing = true
    }

    private fun showNotFoundMessage() = with(binding) {
        hideLoading()
        pupilList.visibility = View.GONE
        notFoundError.visibility = View.VISIBLE
        pupilList.isEnabled = true
    }

    private fun handleSuccessEvent() = with(binding) {
        notFoundError.visibility = View.GONE
        pupilList.visibility = View.VISIBLE
        pupilList.isEnabled = true
        hideLoading()
    }

    private fun hideLoading() = with(binding) {
        swipeRefresh.isRefreshing = false
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