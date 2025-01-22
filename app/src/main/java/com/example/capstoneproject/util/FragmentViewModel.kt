package com.example.capstoneproject.util

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FragmentViewModel : ViewModel() {
    val isDrawerOpen = MutableLiveData<Boolean>()
    val profileImageUrl = MutableLiveData<Uri>()
}