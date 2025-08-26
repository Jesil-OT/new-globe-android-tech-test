package com.bridge.androidtechnicaltest.feature.edit_pupil

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.core.utils.ui.provideGlide
import com.bridge.androidtechnicaltest.databinding.FragmentEditpupilBinding
import com.bridge.androidtechnicaltest.feature.add_pupil.model.countries
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUI
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class PupilEditFragment : Fragment(R.layout.fragment_editpupil), TextWatcher {
    private var _binding: FragmentEditpupilBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PupilEditViewModel by viewModel()
    private val args by navArgs<PupilEditFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditpupilBinding.bind(view)
        setUpObservers()
        setUpView()
        setUpAction()
    }

    private fun setUpTextFields(pupil: PupilUI) = with(binding) {
        firstNameTextField.setText(pupil.pupilName.split(" ").getOrNull(0) ?: "")
        lastNameTextField.setText(pupil.pupilName.split(" ").getOrNull(1) ?: "")
        idTextField.setText(pupil.pupilId)
        countryTextField.setText(pupil.pupilCountry)
        root.provideGlide(image = pupilImage, load = pupil.pupilImage)
    }

    private fun setUpView() = with(binding) {
        idTextField.addTextChangedListener(this@PupilEditFragment)
        firstNameTextField.addTextChangedListener(this@PupilEditFragment)
        lastNameTextField.addTextChangedListener(this@PupilEditFragment)
        countryTextField.addTextChangedListener(this@PupilEditFragment)
        setUpCountryAdapter()
        viewModel.getPupilToEdit(args.pupilId)
    }

    private fun setUpAction() = with(binding) {
        saveButton.setOnClickListener {
            val pupilId = idTextField.text.toString()
            val pupilFirstName = firstNameTextField.text.toString()
            val pupilLastName = lastNameTextField.text.toString()
            val pupilCountry = countryTextField.text.toString()
            viewModel.updatePupil(
                pupilId = pupilId,
                pupilFirstname = pupilFirstName,
                pupilLastName = pupilLastName,
                pupilCountry = pupilCountry,
                pupilImage = ""
            )
        }
        cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setUpCountryAdapter() = with(binding) {
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.country_item, countries)
        countryTextField.setAdapter(arrayAdapter)
    }

    private fun setUpObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.pupil.collect { uiResponse ->
                        setUpTextFields(uiResponse)
                    }
                }

                launch {
                    viewModel.responseUiState.collect { uiResponse ->
                        when (uiResponse) {
                            is PupilEditViewModel.EditPupilUIResponse.Idle -> {}
                            is PupilEditViewModel.EditPupilUIResponse.PupilEdited -> successState(
                                getString(
                                    R.string.pupil_updated
                                )
                            )
                            is PupilEditViewModel.EditPupilUIResponse.ErrorEditingPupil -> errorState(
                                uiResponse.errorMessage
                            )
                            is PupilEditViewModel.EditPupilUIResponse.Loading -> loadingState()
                        }
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

    private fun otherViewState(state: Boolean) = with(binding) {
        saveButton.isEnabled = state
        nameTextFieldLayout.isEnabled = state
        lastNameTextFieldLayout.isEnabled = state
        countryTextFieldLayout.isEnabled = state
        cancelButton.isEnabled = state
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            saveButton.isEnabled =
                !idTextField.text.isNullOrBlank() &&
                        !firstNameTextField.text.isNullOrBlank()
                        && !lastNameTextField.text.isNullOrBlank()
                        && !countryTextField.text.isNullOrBlank()
        }
    }

    override fun afterTextChanged(s: Editable?) {}
}