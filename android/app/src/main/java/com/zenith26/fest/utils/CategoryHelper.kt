package com.zenith26.fest.utils

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.zenith26.fest.R

object CategoryHelper {

    fun applyCategoryBadge(textView: TextView, category: String) {
        val context = textView.context
        val catLower = category.trim().lowercase()

        val (bgRes, textColorRes) = when (catLower) {
            "technical" -> Pair(R.drawable.bg_badge_technical, R.color.cat_tech_text)
            "cultural" -> Pair(R.drawable.bg_badge_cultural, R.color.cat_cultural_text)
            "sports" -> Pair(R.drawable.bg_badge_sports, R.color.cat_sports_text)
            "gaming" -> Pair(R.drawable.bg_badge_gaming, R.color.cat_gaming_text)
            "workshop" -> Pair(R.drawable.bg_badge_workshop, R.color.cat_workshop_text)
            else -> Pair(R.drawable.bg_badge_default, R.color.cat_default_text)
        }

        textView.text = category
        textView.setBackgroundResource(bgRes)
        textView.setTextColor(ContextCompat.getColor(context, textColorRes))
    }
}
