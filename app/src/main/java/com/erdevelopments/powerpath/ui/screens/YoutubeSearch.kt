package com.erdevelopments.powerpath.ui.screens

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri

internal fun openYoutubeSearch(context: Context, query: String) {
    val trimmedQuery = query.trim()
    if (trimmedQuery.isEmpty()) return

    val youtubeAppIntent = Intent(Intent.ACTION_SEARCH).apply {
        setPackage("com.google.android.youtube")
        putExtra(SearchManager.QUERY, trimmedQuery)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val fallbackIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(trimmedQuery)}")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val intentToLaunch =
        if (youtubeAppIntent.resolveActivity(context.packageManager) != null) {
            youtubeAppIntent
        } else {
            fallbackIntent
        }

    context.startActivity(intentToLaunch)
}
