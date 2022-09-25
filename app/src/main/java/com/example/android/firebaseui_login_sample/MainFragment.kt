/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.firebaseui_login_sample

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.android.firebaseui_login_sample.databinding.FragmentMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.squareup.picasso.Picasso
import java.lang.Exception

class MainFragment : Fragment() {

    companion object {
        const val TAG = "MainFragment"
        const val SIGN_IN_RESULT_CODE = 1001
        const val PLEASE_LOGIN_TEXT = "We don't know you. Please login"
        const val DEFAULT_AVATAR_LINK =
            "https://4xucy2kyby51ggkud2tadg3d-wpengine.netdna-ssl.com/wp-content/uploads/sites/37/2017/02/IAFOR-Blank-Avatar-Image.jpg"

    }

    // Get a reference to the ViewModel scoped to this Fragment
    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: FragmentMainBinding

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        navController = findNavController()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAuthenticationState()

        binding.authButton.setOnClickListener {
            launchSignInFlow()
        }

        binding.deleteButton.setOnClickListener {
            deleteAccountFlow()
        }

        binding.changeEmailButton.setOnClickListener {
            changeEmailFlow()
        }

        binding.changeNameButton.setOnClickListener {
            changeNameFlow()
        }

        binding.changePhoto.setOnClickListener {
            changePhotoFlow()
        }

        binding.securityButton.setOnClickListener {
            navigateToSecurity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // TODO Listen to the result of the sign in process by filter for when
        //  SIGN_IN_REQUEST_CODE is passed back. Start by having log statements to know
        //  whether the user has signed in successfully
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in.
                Log.i(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                // Sign in failed. If response is null, the user canceled the
                // sign-in flow using the back button. Otherwise, check
                // the error code and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    /**
     * Observes the authentication state and changes the UI accordingly.
     * If there is a logged in user: (1) show a logout button and (2) display their name.
     * If there is no logged in user: show a login button
     */
    private fun observeAuthenticationState() {

        // TODO Use the authenticationState variable from LoginViewModel to update the UI
        //  accordingly.
        //
        //  TODO If there is a logged-in user, authButton should display Logout. If the
        //   user is logged in, you can customize the welcome message by utilizing
        //   getFactWithPersonalition(). I

        // TODO If there is no logged in user, authButton should display Login and launch the sign
        //  in screen when clicked. There should also be no personalization of the message
        //  displayed.

        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    onLogin()
                }
                else -> {
                    onLogout()
                }
            }
        })
    }


    private fun getContentForAuth(): String {
        val provider = FirebaseAuth.getInstance().getAccessToken(false).result?.signInProvider
        var name = viewModel.userName
        if (name.isEmpty()) {
            name = "-"
        }
        return String.format(
            resources.getString(
                R.string.welcome_message_authed,
                name,
                provider
            )
        )
    }

    private fun launchSignInFlow() {
        // TODO Complete this function by allowing users to register and sign in with
        //  either their email address or Google account.
        // Give users the option to sign in / register with their email or Google account.
        // If users choose to register with their email,
        // they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()

            // This is where you can provide more ways for users to register and
            // sign in.
        )

        // Create and launch the sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_REQUEST_CODE.
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_RESULT_CODE
        )
    }

    private fun deleteAccountFlow() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("deleteAccountFlow", "user was null")
            return
        }
        user.delete()
        Log.i("deleteAccountFlow", "user $user deleted")
        onLogout()
    }

    private fun changeEmailFlow() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("changeEmailFlow", "user was null")
            return
        }
        val oldEmail = user.email
        val newEmail = binding.emailInput.text.toString()
        if (newEmail.isEmpty()) {
            return
        }

        try {
            user.updateEmail(newEmail)
        } catch (e: FirebaseException) {
            Toast.makeText(requireContext(), "Firebase exception ${e.message}", Toast.LENGTH_LONG)
                .show()
            return
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unknown exception ${e.message}", Toast.LENGTH_LONG)
                .show()
            return
        }

        Toast.makeText(requireContext(), "Email updated $oldEmail → $newEmail", Toast.LENGTH_LONG)
            .show()
    }

    private fun changeNameFlow() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("changeNameFlow", "user was null")
            return
        }
        var oldName = user.displayName
        if (oldName.isNullOrEmpty()) {
            oldName = "-"
        }

        val newName = binding.nameInput.text.toString()
        if (newName.isEmpty()) {
            return
        }

        try {
            val updates = UserProfileChangeRequest.Builder().setDisplayName(newName).build()
            user.updateProfile(updates)
            viewModel.userName = newName
            binding.welcomeText.text = getContentForAuth()
        } catch (e: FirebaseException) {
            Toast.makeText(requireContext(), "Firebase exception ${e.message}", Toast.LENGTH_LONG)
                .show()
            return
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unknown exception ${e.message}", Toast.LENGTH_LONG)
                .show()
            return
        }

        Toast.makeText(requireContext(), "Name updated $oldName → $newName", Toast.LENGTH_LONG)
            .show()
    }

    private fun changePhotoFlow() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("changePhotoFlow", "user was null")
            return
        }

        val newPhoto = binding.photoInput.text.toString()
        if (newPhoto.isEmpty()) {
            return
        }

        try {
            val updates =
                UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(newPhoto)).build()
            user.updateProfile(updates)
            viewModel.userPhoto = newPhoto
            initPhoto()
        } catch (e: FirebaseException) {
            Toast.makeText(requireContext(), "Firebase exception ${e.message}", Toast.LENGTH_LONG)
                .show()
            return
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unknown exception ${e.message}", Toast.LENGTH_LONG)
                .show()
            return
        }

        Toast.makeText(requireContext(), "Photo updated", Toast.LENGTH_LONG)
            .show()
    }

    private fun navigateToSecurity() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("navigateToSecurity", "user was null. forbidden")
            return
        }

        findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
    }

    private fun onLogout() {
        binding.authButton.text = getString(R.string.login_button_text)
        binding.authButton.setOnClickListener { launchSignInFlow() }
        binding.welcomeText.text = PLEASE_LOGIN_TEXT
        changeVisibility(false)
        viewModel.userName = ""
        viewModel.userPhoto = ""
    }

    private fun initPhoto() {
        Picasso.with(requireContext()).load(viewModel.userPhoto).into(binding.imageView)
    }

    private fun initUserData() {
        val user = FirebaseAuth.getInstance().currentUser

        viewModel.userName = user?.displayName ?: ""

        var url = user?.photoUrl?.toString()?.replace("s96-c", "s400-c")
        if (url.isNullOrEmpty()) {
            url = DEFAULT_AVATAR_LINK
        }

        viewModel.userPhoto = url

        initPhoto()

        binding.emailInput.setText(user?.email ?: "")
        binding.nameInput.setText(user?.displayName ?: "")
    }

    private fun onLogin() {
        initUserData()

        binding.authButton.text = getString(R.string.logout_button_text)
        binding.authButton.setOnClickListener {
            AuthUI.getInstance().signOut(requireContext())
        }

        binding.welcomeText.text = getContentForAuth()

        changeVisibility(true)
    }

    private fun changeVisibility(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.INVISIBLE

        binding.imageView.visibility = visibility
        binding.deleteButton.visibility = visibility
        binding.emailInput.visibility = visibility
        binding.changeEmailButton.visibility = visibility
        binding.nameInput.visibility = visibility
        binding.changeNameButton.visibility = visibility
        binding.photoInput.visibility = visibility
        binding.changePhoto.visibility = visibility
        binding.securityButton.visibility = visibility
    }
}