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
package com.android.settings.dotextras.custom.sections

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.settings.dotextras.BaseActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.stats.Constants
import com.android.settings.dotextras.custom.utils.SettingsConstants
import com.android.settings.dotextras.custom.views.DotMaterialPreference
import com.google.android.material.snackbar.Snackbar

class SettingsSection : GenericSection() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.section_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as BaseActivity).setTitle(getString(R.string.section_settings))
        val sharedprefStats = requireActivity().getSharedPreferences("dotStatsPrefs", Context.MODE_PRIVATE)
        val prefStats: DotMaterialPreference = view.findViewById(R.id.pref_stats)
        prefStats.switchView!!.isChecked = sharedprefStats.getBoolean(Constants.ALLOW_STATS, true)
        prefStats.setOnClickPreference {
            prefStats.switchView!!.isChecked = !prefStats.switchView!!.isChecked
            val editor: SharedPreferences.Editor = sharedprefStats.edit()
            editor.putBoolean(Constants.ALLOW_STATS, prefStats.switchView!!.isChecked)
            editor.apply()
            if (prefStats.switchView!!.isChecked && sharedprefStats.getBoolean(Constants.IS_FIRST_LAUNCH, true))
                Snackbar.make(view, "Stats will be pushed on the next launch!", Snackbar.LENGTH_SHORT).show()
        }
        val pref = requireActivity().getSharedPreferences(SettingsConstants.SETTINGS_PREF, Context.MODE_PRIVATE)
        buildSetting(pref, view.findViewById(R.id.pref_rgb_light), SettingsConstants.ALLOW_RGB_BLIGHT)
        buildSetting(pref, view.findViewById(R.id.pref_balloons), SettingsConstants.SHOW_BALLOONS, true)
    }

    private fun buildSetting(preferences: SharedPreferences, view: DotMaterialPreference, setting: String) {
        view.switchView!!.isChecked = preferences.getBoolean(setting, false)
        view.setOnClickPreference {
            view.switchView!!.isChecked = !view.switchView!!.isChecked
            val editor: SharedPreferences.Editor = preferences.edit()
            editor.putBoolean(setting, view.switchView!!.isChecked)
            editor.apply()
        }
    }

    private fun buildSetting(preferences: SharedPreferences, view: DotMaterialPreference, setting: String, default: Boolean) {
        view.switchView!!.isChecked = preferences.getBoolean(setting, default)
        view.setOnClickPreference {
            view.switchView!!.isChecked = !view.switchView!!.isChecked
            val editor: SharedPreferences.Editor = preferences.edit()
            editor.putBoolean(setting, view.switchView!!.isChecked)
            editor.apply()
        }
    }
}