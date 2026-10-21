package com.example.daftar

import android.app.Application

class App: Application() {
    val container : AppContainer by lazy {
        AppContainer(this)
    }
}