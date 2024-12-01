package com.tutortoise.tutortoise.data.pref

import androidx.credentials.GetCredentialRequest
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.tutortoise.tutortoise.BuildConfig

class OAuthConfig {
    private val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.GOOGLE_OAUTH_CLIENT_ID)
        .setNonce(null)
        .build()

    fun getCredentialRequest(): GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(this.googleIdOption)
        .build()

    // GoogleSignInOptions for fallback
    fun getGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_OAUTH_CLIENT_ID)
            .requestEmail()
            .build()
    }
}