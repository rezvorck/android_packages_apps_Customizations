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
package com.android.settings.dotextras.custom.sections.wallpaper.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.settings.dotextras.custom.sections.wallpaper.WallpaperBase

class CurrentWallpaperAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private var wallpaperBase: WallpaperBase? = null
    private var fragmentList = arrayListOf(
        HomeWallpaperFragment(),
        LockscreenWallpaperFragment()
    )

    constructor(fragmentActivity: FragmentActivity, wallpaperBase: WallpaperBase) : this(
        fragmentActivity
    ) {
        this.wallpaperBase = wallpaperBase
        fragmentList = arrayListOf(
            HomeWallpaperFragment(wallpaperBase),
            LockscreenWallpaperFragment(wallpaperBase)
        )
    }

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}