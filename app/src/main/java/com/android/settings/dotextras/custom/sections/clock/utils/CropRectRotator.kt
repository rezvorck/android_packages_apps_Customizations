/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.settings.dotextras.custom.sections.clock.utils

import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import androidx.exifinterface.media.ExifInterface

/**
 * Rotates crop rectangles for bitmap region operations on rotated images (i.e., with non-normal
 * EXIF orientation).
 */
object CropRectRotator {
    private const val TAG = "CropRectRotator"

    /**
     * Rotates and returns a new crop Rect which is adjusted for the provided EXIF orientation value.
     */
    fun rotateCropRectForExifOrientation(
        dimensions: Point, srcRect: Rect,
        exifOrientation: Int,
    ): Rect {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_NORMAL -> Rect(srcRect)
            ExifInterface.ORIENTATION_ROTATE_90 -> Rect(srcRect.top,
                dimensions.x - srcRect.right,
                srcRect.bottom,
                dimensions.x - srcRect.left)
            ExifInterface.ORIENTATION_ROTATE_180 -> Rect(dimensions.x - srcRect.right,
                dimensions.y - srcRect.bottom,
                dimensions.x - srcRect.left,
                dimensions.y - srcRect.top)
            ExifInterface.ORIENTATION_ROTATE_270 -> Rect(dimensions.y - srcRect.bottom,
                srcRect.left,
                dimensions.y - srcRect.top,
                srcRect.right)
            else -> {
                Log.w(TAG, "Unsupported EXIF orientation $exifOrientation")
                Rect(srcRect)
            }
        }
    }
}