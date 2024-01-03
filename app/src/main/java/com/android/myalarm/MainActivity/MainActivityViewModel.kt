package com.android.myalarm.MainActivity

import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel(){

    /** variable that tracks that the dialog was displayed to once in the current lifecycle */
    var isDialogShown = false
}