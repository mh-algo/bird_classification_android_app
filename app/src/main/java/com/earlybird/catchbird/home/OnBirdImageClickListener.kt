package com.earlybird.catchbird.home

import android.view.View

interface OnBirdImageClickListener {
    fun onItemClick(holder:ClassificationAdapter.ViewHolder?, view: View?, position: Int)
}