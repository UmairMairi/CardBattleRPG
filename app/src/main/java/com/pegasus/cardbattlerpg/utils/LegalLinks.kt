package com.pegasus.cardbattlerpg.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

// Public pages required by Google Play (privacy policy, UGC terms, account deletion).
// Host the files from /legal on this domain before submitting the app.
object LegalLinks {
    const val WEBSITE = "https://cardbattlerpg.online/"
    const val PRIVACY_POLICY = "https://cardbattlerpg.online/privacy-policy"
    const val TERMS_OF_USE = "https://cardbattlerpg.online/terms"
    const val DELETE_ACCOUNT = "https://cardbattlerpg.online/delete-account"

    fun open(context: Context, url: String) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
