package com.tutortoise.tutortoise.data.pref

import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.tutortoise.tutortoise.BuildConfig

class OAuthConfig {
    private val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.GOOGLE_OAUTH_CLIENT_ID)
        .build()

    fun getCredentialRequest(): GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(this.googleIdOption)
        .build()
}