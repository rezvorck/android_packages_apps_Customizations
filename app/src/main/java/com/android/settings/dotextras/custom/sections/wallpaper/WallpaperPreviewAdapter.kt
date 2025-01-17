/*
 * Copyright (C) 2020 The dotOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.dotextras.custom.sections.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ContentResolver
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.DisplayMetrics
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.wallpaper.fragments.ApplyDialogFragment
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.card.MaterialCardView
import java.io.IOException

typealias onDismiss = (() -> Unit)?
typealias onShow = (() -> Unit)?

class WallpaperPreviewAdapter(
    private var items: ArrayList<WallpaperBase>,
    private val wallpaperManager: WallpaperManager,
    private val fragment: Fragment,
    private val pager: ViewPager2,
    private val dismissListener: onDismiss,
) : RecyclerView.Adapter<WallpaperPreviewAdapter.ViewHolder>() {

    private val SELECT_PICTURE = 1

    private lateinit var selectedImagePath: String

    private lateinit var contentResolver: ContentResolver

    private lateinit var synteticActivity: Activity

    private lateinit var wallpaperBase: WallpaperBase

    private var listener: onWallpaperChanged = null

    fun updateList(list: ArrayList<WallpaperBase>) {
        items = list
    }

    private val getContent =
        fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val drawable = Drawable.createFromStream(
                    contentResolver.openInputStream(uri),
                    uri.toString()
                )
                val display: DisplayMetrics = synteticActivity.resources.displayMetrics
                val bitmapDrawable = scaleCropToFit(drawableToBitmap(drawable)!!,
                    display.widthPixels,
                    display.heightPixels)
                val wallpaperGallery =
                    WallpaperBase(bitmapDrawable!!.toDrawable(synteticActivity.resources))
                wallpaperGallery.type = wallpaperGallery.GALLERY
                wallpaperGallery.listener = listener
                wallpaperGallery.onPressed = wallpaperBase.onPressed
                wallpaperGallery.uri = uri
                val dialog = ApplyDialogFragment(wallpaperGallery, pager.currentItem)
                dialog.dismissListener = dismissListener
                dialog.show(
                    fragment.parentFragmentManager,
                    "${wallpaperGallery.type}"
                )
                wallpaperGallery.onPressed?.invoke()
            }
        }

    private fun scaleCropToFit(original: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap? {
        val width = original.width
        val height = original.height
        val widthScale = targetWidth.toFloat() / width.toFloat()
        val heightScale = targetHeight.toFloat() / height.toFloat()
        val scaledWidth: Float
        val scaledHeight: Float
        var startY = 0
        var startX = 0
        if (widthScale > heightScale) {
            scaledWidth = targetWidth.toFloat()
            scaledHeight = height * widthScale
            startY = ((scaledHeight - targetHeight) / 2).toInt()
        } else {
            scaledHeight = targetHeight.toFloat()
            scaledWidth = width * heightScale
            startX = ((scaledWidth - targetWidth) / 2).toInt()
        }
        val scaledBitmap = Bitmap.createScaledBitmap(
            original,
            scaledWidth.toInt(), scaledHeight.toInt(), true
        )
        return Bitmap.createBitmap(scaledBitmap, startX, startY, targetWidth, targetHeight)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_wallpaper_preview,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wallpaper: WallpaperBase = items[position]
        wallpaperBase = wallpaper
        contentResolver = holder.wallpaperPreview.context.contentResolver
        synteticActivity = holder.wallpaperPreview.context as Activity
        this.listener = wallpaper.listener
        if (wallpaper.title != null) holder.title.text = wallpaper.title
        else holder.title.visibility = View.GONE
        when (wallpaper.type) {
            wallpaper.GALLERY -> {
                holder.wallpaperPreview.setImageDrawable(wallpaper.drawable)
                holder.wallpaperPreview.scaleType = ImageView.ScaleType.CENTER_INSIDE
                holder.wallpaperPreview.imageTintList =
                    ColorStateList.valueOf(ResourceHelper.getSecondaryTextColor(synteticActivity))
                holder.wallpaperPreview.setOnClickListener {
                    getContent.launch("image/*")
                }
            }
            wallpaper.SYSTEM -> {
                holder.wallpaperPreview.setImageDrawable(wallpaper.drawable)
                holder.wallpaperPreview.setOnClickListener {
                    wallpaper.onPressed?.invoke()
                    val dialog = ApplyDialogFragment(wallpaper, pager.currentItem)
                    dialog.dismissListener = dismissListener
                    dialog.show(
                        fragment.parentFragmentManager,
                        "${wallpaper.type}"
                    )
                }
            }
            wallpaper.WEB -> {
                Glide.with(holder.wallpaperPreview)
                    .load(Uri.parse(wallpaper.url))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .thumbnail(0.1f)
                    .into(holder.wallpaperPreview)
                holder.wallpaperPreview.setOnClickListener {
                    wallpaper.onPressed?.invoke()
                    val dialog = ApplyDialogFragment(wallpaper, pager.currentItem)
                    dialog.dismissListener = dismissListener
                    dialog.show(
                        fragment.parentFragmentManager,
                        "${wallpaper.type}"
                    )
                }
            }
        }
    }

    private fun setWallpaper(bitmap: Bitmap) {
        val metrics = DisplayMetrics()
        val display: Display = synteticActivity.windowManager.defaultDisplay
        display.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        wallpaperManager.suggestDesiredDimensions(screenWidth, screenHeight)
        val width = wallpaperManager.desiredMinimumWidth
        val height = wallpaperManager.desiredMinimumHeight
        val wallpaper = Bitmap.createScaledBitmap(bitmap, width, height, true)
        try {
            wallpaperManager.bitmap = wallpaper
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setWallpaper(drawable: Drawable) {
        val metrics = DisplayMetrics()
        val display: Display = synteticActivity.windowManager.defaultDisplay
        display.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        wallpaperManager.suggestDesiredDimensions(screenWidth, screenHeight)
        val width = wallpaperManager.desiredMinimumWidth
        val height = wallpaperManager.desiredMinimumHeight
        val wallpaper = Bitmap.createScaledBitmap(drawableToBitmap(drawable)!!, width, height, true)
        try {
            wallpaperManager.bitmap = wallpaper
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }
        val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            )
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.wallpaperTitle)
        val wallpaperPreview: ImageButton = view.findViewById(R.id.wallpaper_preview)
        val wallpaperHolder: MaterialCardView = view.findViewById(R.id.wall_holder)
    }

}