package com.bridge.androidtechnicaltest.feature.add_pupil

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
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.core.utils.ui.provideGlide
import com.bridge.androidtechnicaltest.databinding.FragmentAddpupilBinding
import com.bridge.androidtechnicaltest.feature.add_pupil.model.countries
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddPupilFragment : Fragment(R.layout.fragment_addpupil), TextWatcher {
    private var _binding: FragmentAddpupilBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddPupilViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddpupilBinding.bind(view)
        setUpObservers()
        setUpViews()
        setUpActions()
    }

    private fun setUpViews() = with(binding) {
        root.provideGlide(image = pupilImage, load = "")
        firstNameTextField.addTextChangedListener(this@AddPupilFragment)
        lastNameTextField.addTextChangedListener(this@AddPupilFragment)
        editCountryTextField.addTextChangedListener(this@AddPupilFragment)
        setUpCountryAdapter()
    }

    private fun setUpActions() = with(binding) {
        createPupilButton.setOnClickListener {
            val pupilFirstName = firstNameTextField.text.toString()
            val pupilLastName = lastNameTextField.text.toString()
            val pupilCountry = editCountryTextField.text.toString()
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
                viewModel.responseUiState.collect { uiResponse ->
                    when (uiResponse) {
                        is AddPupilUIResponse.Idle -> {}
                        is AddPupilUIResponse.PupilAdded -> successState(
                            getString(
                                R.string.new_pupil_created,
                                uiResponse.pupilName
                            )
                        )
                        is AddPupilUIResponse.ErrorAddingPupil -> errorState(uiResponse.errorMessage)
                        is AddPupilUIResponse.Loading -> loadingState()
                    }
                }
            }
        }
    }

    private fun loadingState() {
        binding.loadingView.visibility = View.VISIBLE
        otherViewState(state = false)
    }

    private fun errorState(errorMessage: String) {
        binding.loadingView.visibility = View.GONE
        otherViewState(state = true)
        Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_SHORT).show()
    }

    private fun successState(message: String) {
        binding.loadingView.visibility = View.GONE
        otherViewState(state = true)
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun otherViewState(state: Boolean) = with(binding){
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