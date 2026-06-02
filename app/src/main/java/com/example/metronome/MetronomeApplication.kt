package com.example.metronome

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class required by Hilt to generate the dependency injection components.
 */
@HiltAndroidApp
class MetronomeApplication : Application()