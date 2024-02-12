package com.dr.qck.application

import android.app.Application
import android.graphics.Bitmap
import com.dr.qck.database.ExceptionDao
import com.dr.qck.datastore.DatastoreRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class QckApplication : Application() {

    @Inject
    lateinit var dao: ExceptionDao

    @Inject
    lateinit var repo: DatastoreRepository

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        var isThemeSwitched = Pair<Boolean, String?>(false, null)
        lateinit var instance: QckApplication
        var snapshot: Bitmap? = null
    }
}