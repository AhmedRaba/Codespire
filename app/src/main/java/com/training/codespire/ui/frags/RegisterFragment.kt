package com.training.codespire.ui.frags

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.training.codespire.MainActivity
import com.training.codespire.R
import com.training.codespire.data.datastore.SharedPreferencesUtil
import com.training.codespire.data.viewmodel.AuthViewmodel
import com.training.codespire.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var authViewModel: AuthViewmodel
    private lateinit var sharedPreferencesUtil: SharedPreferencesUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        sharedPreferencesUtil = SharedPreferencesUtil(requireContext())

        authViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(AuthViewmodel::class.java)

        authViewModel.registerResponseLiveData.observe(viewLifecycleOwner) { registerResponse ->
            showLoading()
            registerResponse?.let {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                // Automatically log in the user after successful registration
                val email = binding.etRegEmail.text.toString()
                val password = binding.etRegPassword.text.toString()
                authViewModel.loginUser(email, password)
            }
        }

        authViewModel.loginResponseLiveData.observe(viewLifecycleOwner) { loginResponse ->
            showLoading()
            loginResponse?.let {

                sharedPreferencesUtil.isLoggedIn = true
                sharedPreferencesUtil.token = loginResponse.token
                Log.e("authViewModel", sharedPreferencesUtil.token.toString())
                // Navigate to the home screen
                navigateToHome()
            }
        }

        authViewModel.errorLiveData.observe(viewLifecycleOwner) { error ->
            error?.let {
                hideLoading()
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        navigateToLogin()
        checkRegister()

        return binding.root
    }

    private fun navigateToHome() {

        val intent = Intent(requireContext(), MainActivity::class.java)
        requireActivity().startActivity(intent)
        requireActivity().finish()
    }

    private fun navigateToLogin() {
        binding.tvRegSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun checkRegister() {
        binding.btnRegister.setOnClickListener {
            val username = binding.etRegUsername.text.toString()
            val email = binding.etRegEmail.text.toString()
            val password = binding.etRegPassword.text.toString()
            val confirmPassword = binding.etRegConfirmPassword.text.toString()
            Log.d("RegisterFragment", "Password: $password")
            Log.d("RegisterFragment", "Confirm Password: $confirmPassword")

            if (validateInput(username, email, password, confirmPassword)) {
                showLoading()
                authViewModel.registerUser(
                    username = username,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword
                )
            }
        }

        setupPasswordVisibilityToggle(
            binding.etRegPassword, R.drawable.ic_eye, R.drawable.ic_eye_off
        )
        setupPasswordVisibilityToggle(
            binding.etRegConfirmPassword, R.drawable.ic_eye, R.drawable.ic_eye_off
        )
    }

    private fun validateInput(
        username: String, email: String, password: String, confirmPassword: String
    ): Boolean {
        val trimmedPassword = password.trim()
        val trimmedConfirmPassword = confirmPassword.trim()

        var isValid = true

        if (username.isEmpty()) {
            setFieldError(binding.etRegUsername, binding.tvRegUsernameError, "Username is required")
            isValid = false
        } else {
            setFieldNormal(binding.etRegUsername, binding.tvRegUsernameError)
        }

        if (email.isEmpty()) {
            setFieldError(binding.etRegEmail, binding.tvRegEmailError, "Email is required")
            isValid = false
        } else {
            setFieldNormal(binding.etRegEmail, binding.tvRegEmailError)
        }

        if (trimmedPassword.isEmpty()) {
            setFieldError(binding.etRegPassword, binding.tvRegPasswordError, "Password is required")
            isValid = false
        } else {
            setFieldNormal(binding.etRegPassword, binding.tvRegPasswordError)
        }

        if (trimmedConfirmPassword.isEmpty()) {
            setFieldError(
                binding.etRegConfirmPassword,
                binding.tvRegConfirmPasswordError,
                "Password is required"
            )
            isValid = false
        } else if (trimmedPassword != trimmedConfirmPassword) {
            setFieldError(
                binding.etRegConfirmPassword,
                binding.tvRegConfirmPasswordError,
                "Password doesn't match"
            )
            isValid = false
        } else {
            setFieldNormal(binding.etRegConfirmPassword, binding.tvRegConfirmPasswordError)
        }

        return isValid
    }

    private fun setFieldError(
        field: EditText, errorTextView: TextView, errorMessage: String
    ) {
        errorTextView.visibility = View.VISIBLE
        errorTextView.text = errorMessage
        field.setBackgroundResource(R.drawable.et_border_error)
    }

    private fun setFieldNormal(field: EditText, errorTextView: TextView) {
        errorTextView.visibility = View.GONE
        field.setBackgroundResource(R.drawable.et_border_selector)
    }

    private fun setupPasswordVisibilityToggle(
        passwordField: EditText, visibleIcon: Int, hiddenIcon: Int
    ) {
        passwordField.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2 // index for drawableRight
                val drawable = passwordField.compoundDrawables[drawableEnd]

                if (drawable != null && event.rawX >= (passwordField.right - drawable.bounds.width())) {
                    val selection = passwordField.selectionEnd
                    if (passwordField.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                        passwordField.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, hiddenIcon, 0)
                    } else {
                        passwordField.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, visibleIcon, 0)
                    }
                    passwordField.setSelection(selection)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    private fun showLoading() {
        binding.registerFragment.visibility = View.GONE
        binding.progressBarRegister.visibility = View.VISIBLE
    }
    private fun hideLoading() {
        binding.registerFragment.visibility = View.VISIBLE
        binding.progressBarRegister.visibility = View.GONE
    }

}
