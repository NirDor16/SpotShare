package com.example.spotshare

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavigationViewHelper {
    fun setup(bottomNav: BottomNavigationView, currentActivity: Activity) {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (currentActivity !is MainActivity) {
                        currentActivity.startActivity(Intent(currentActivity, MainActivity::class.java))
                        currentActivity.finish()
                    }
                    true
                }

                R.id.nav_add_post -> {
                    if (currentActivity !is AddPostActivity) {
                        currentActivity.startActivity(Intent(currentActivity, AddPostActivity::class.java))
                        currentActivity.finish()
                    }
                    true
                }

                R.id.nav_profile -> {
                    if (currentActivity !is ProfileActivity) {
                        currentActivity.startActivity(Intent(currentActivity, ProfileActivity::class.java))
                        currentActivity.finish()
                    }
                    true
                }

                R.id.nav_favorites -> {
                    if (currentActivity !is FavoritesActivity) {
                        currentActivity.startActivity(Intent(currentActivity, FavoritesActivity::class.java))
                        currentActivity.finish()
                    }
                    true
                }

                else -> false
            }
        }
    }
}
