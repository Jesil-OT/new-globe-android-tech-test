package com.bridge.androidtechnicaltest.core.utils.ui

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bridge.androidtechnicaltest.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

fun provideRequestOption(): RequestOptions {
    return RequestOptions()
        .placeholder(R.drawable.ic_sync)
        .error(R.drawable.ic_error)
}

fun View.provideGlide(image: ImageView, load: String) =
    Glide.with(this)
        .load(load)
        .apply(provideRequestOption())
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(image)

