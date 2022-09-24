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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.android.firebaseui_login_sample.databinding.FragmentMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.squareup.picasso.Picasso

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAuthenticationState()

        binding.authButton.setOnClickListener {
            // TODO call launchSignInFlow when authButton is clicked
            launchSignInFlow()
        }

        binding.deleteButton.setOnClickListener {
            deleteAccountFlow()
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
                    binding.authButton.text = getString(R.string.logout_button_text)
                    binding.authButton.setOnClickListener {
                        // TODO implement logging out user in next step
                        AuthUI.getInstance().signOut(requireContext())
                    }

                    // TODO 2. If the user is logged in,
                    // you can customize the welcome message they see by
                    // utilizing the getFactWithPersonalization() function provided
                    binding.welcomeText.text = getContentForAuth()
                    var url = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
                        ?.replace("s96-c", "s400-c")
                    if (url == null) {
                        url = DEFAULT_AVATAR_LINK
                    }
                    Picasso.with(this.requireContext()).load(url).into(binding.imageView)
                    binding.imageView.visibility = View.VISIBLE
                    binding.deleteButton.visibility = View.VISIBLE

                   // FirebaseAuth.getInstance().currentUser?.updateEmail("ds-drozdov@yandex.ru")

//                    val updates =
//                        UserProfileChangeRequest.Builder().setDisplayName("Oaoaoaa").build()
//                    FirebaseAuth.getInstance().currentUser?.updateProfile(updates)
                }
                else -> {
                    // TODO 3. Lastly, if there is no logged-in user,
                    // auth_button should display Login and
                    //  launch the sign in screen when clicked.
                    onLogout()
                }
            }
        })
    }


    private fun getContentForAuth(): String {
        val provider = FirebaseAuth.getInstance().getAccessToken(false).result?.signInProvider
        var name = FirebaseAuth.getInstance().currentUser?.displayName ?: "-"
        if (name == "") {
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
        onLogout()
    }

    private fun onLogout() {
        binding.authButton.text = getString(R.string.login_button_text)
        binding.authButton.setOnClickListener { launchSignInFlow() }
        binding.welcomeText.text = PLEASE_LOGIN_TEXT
        binding.imageView.visibility = View.INVISIBLE
        binding.deleteButton.visibility = View.INVISIBLE
    }
}