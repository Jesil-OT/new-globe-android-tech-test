package com.bridge.androidtechnicaltest.feature.edit_pupil.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.core.utils.ui.Utils.trimMultipleSpaces
import com.bridge.androidtechnicaltest.core.utils.ui.provideGlide
import com.bridge.androidtechnicaltest.databinding.FragmentEditpupilBinding
import com.bridge.androidtechnicaltest.feature.create_pupil.model.countries
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
        countryTextField.setText(pupil.pupilCountry)
        root.provideGlide(image = pupilImage, load = pupil.pupilImage)
    }

    private fun setUpView() = with(binding) {
        firstNameTextField.addTextChangedListener(this@PupilEditFragment)
        lastNameTextField.addTextChangedListener(this@PupilEditFragment)
        countryTextField.addTextChangedListener(this@PupilEditFragment)
        setUpCountryAdapter()
        viewModel.getPupilToEdit(args.pupilId)
    }

    private fun setUpAction() = with(binding) {
        saveButton.setOnClickListener {
            val pupilFirstName = firstNameTextField.text.toString().trimMultipleSpaces()
            val pupilLastName = lastNameTextField.text.toString().trimMultipleSpaces()
            val pupilCountry = countryTextField.text.toString().trimMultipleSpaces()
            viewModel.updatePupil(
                pupilId = args.pupilId,
                pupilFirstname = pupilFirstName,
                pupilLastName = pupilLastName,
                pupilCountry = pupilCountry,
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
                    viewModel.handleEventState.collect { event ->
                        when (event) {

                            is PupilEditViewModel.EditPupilOneTimeEvent.ErrorEvent -> handleErrorEvent(event.errorMessage)

                            is PupilEditViewModel.EditPupilOneTimeEvent.LoadingEvent -> handleLoadingEvent()

                            is PupilEditViewModel.EditPupilOneTimeEvent.SuccessEvent -> handleSuccessEvent()

                        }
                    }
                }
            }
        }
    }

    private fun handleSuccessEvent() {
        hideLoading()
        disableOtherViews(state = true)
        findNavController().popBackStack()
        Toast.makeText(requireContext(), getString(R.string.pupil_updated), Toast.LENGTH_SHORT).show()
    }

    private fun handleLoadingEvent() = with(binding) {
        loadingView.visibility = View.VISIBLE
        saveButton.isEnabled = false
        disableOtherViews(state = false)
    }

    private fun handleErrorEvent(
        eventMessage: Int
    ) {
        hideLoading()
        disableOtherViews(state = true)
        Snackbar.make(binding.root, eventMessage, Snackbar.LENGTH_SHORT).show()
    }

    private fun hideLoading() = with(binding) {
        loadingView.visibility = View.GONE
        saveButton.isEnabled = true
    }

    private fun disableOtherViews(state: Boolean) = with(binding) {
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
                !firstNameTextField.text.isNullOrBlank()
                        && !lastNameTextField.text.isNullOrBlank()
                        && !countryTextField.text.isNullOrBlank()
        }
    }

    override fun afterTextChanged(s: Editable?) {}
}