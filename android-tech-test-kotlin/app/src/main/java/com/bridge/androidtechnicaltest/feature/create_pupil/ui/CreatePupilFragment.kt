package com.bridge.androidtechnicaltest.feature.create_pupil.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.core.utils.ui.Utils.trimMultipleSpaces
import com.bridge.androidtechnicaltest.core.utils.ui.provideGlide
import com.bridge.androidtechnicaltest.databinding.FragmentCreatePupilBinding
import com.bridge.androidtechnicaltest.feature.create_pupil.model.countries
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreatePupilFragment : Fragment(R.layout.fragment_create_pupil), TextWatcher {
    private var _binding: FragmentCreatePupilBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddPupilViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreatePupilBinding.bind(view)
        setUpObservers()
        setUpViews()
        setUpActions()
    }

    private fun setUpViews() = with(binding) {
        root.provideGlide(image = pupilImage, load = "")
        firstNameTextField.addTextChangedListener(this@CreatePupilFragment)
        lastNameTextField.addTextChangedListener(this@CreatePupilFragment)
        editCountryTextField.addTextChangedListener(this@CreatePupilFragment)
        setUpCountryAdapter()
    }

    private fun setUpActions() = with(binding) {
        createPupilButton.setOnClickListener {
            val pupilFirstName = firstNameTextField.text.toString().trimMultipleSpaces()
            val pupilLastName = lastNameTextField.text.toString().trimMultipleSpaces()
            val pupilCountry = editCountryTextField.text.toString().trimMultipleSpaces()
            viewModel.addPupil(pupilFirstName, pupilLastName, pupilCountry)
        }
    }

    private fun setUpCountryAdapter() = with(binding) {
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.country_item, countries)
        editCountryTextField.setAdapter(arrayAdapter)
    }

    private fun setUpObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.handleEventState.collect { event ->
                    when (event) {
                        is AddPupilOneTimeEvent.PupilAddedEvent -> handleSuccessEvent(
                            getString(
                                R.string.new_pupil_created,
                                event.pupilName
                            )
                        )

                        is AddPupilOneTimeEvent.ErrorEvent -> handleErrorEvent(event.errorMessage)
                        is AddPupilOneTimeEvent.LoadingEvent -> handleLoadingEvent()
                    }
                }
            }
        }
    }

    private fun handleSuccessEvent(
        eventMessage: String
    ) {
        hideLoading()
        disableOtherViews(state = true)
        findNavController().popBackStack()
        Toast.makeText(requireContext(), eventMessage, Toast.LENGTH_SHORT).show()
    }

    private fun handleErrorEvent(
        eventMessage: Int
    ) {
        hideLoading()
        disableOtherViews(state = true)
        Snackbar.make(binding.root, eventMessage, Snackbar.LENGTH_SHORT).show()
    }

    private fun handleLoadingEvent() = with(binding) {
        loadingView.visibility = View.VISIBLE
        createPupilButton.isEnabled = false
        disableOtherViews(state = false)
    }

    private fun hideLoading() = with(binding) {
        loadingView.visibility = View.GONE
        createPupilButton.isEnabled = true
    }

    private fun disableOtherViews(state: Boolean) = with(binding) {
        createPupilButton.isEnabled = state
        nameTextFieldLayout.isEnabled = state
        lastNameTextFieldLayout.isEnabled = state
        countryTextFieldLayout.isEnabled = state
    }

    override fun beforeTextChanged(
        s: CharSequence?,
        start: Int,
        count: Int,
        after: Int
    ) {
    }

    override fun onTextChanged(
        s: CharSequence?,
        start: Int,
        before: Int,
        count: Int
    ) {
        binding.apply {
            createPupilButton.isEnabled =
                !firstNameTextField.text.isNullOrBlank()
                        && !lastNameTextField.text.isNullOrBlank()
                        && !editCountryTextField.text.isNullOrBlank()
        }
    }

    override fun afterTextChanged(s: Editable?) {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}