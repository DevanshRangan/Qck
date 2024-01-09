package com.dr.qck.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.dr.qck.database.ExceptionDao
import com.dr.qck.database.ExceptionDatabase
import com.dr.qck.datastore.DatastoreRepository
import com.dr.qck.utils.Constants
import com.dr.qck.utils.Constants.DATASTORE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DependencyInjection {

    @Provides
    @Singleton
    fun provideDatastoreRepository(datastore: DataStore<Preferences>): DatastoreRepository {
        return DatastoreRepository(datastore)
    }

    @Provides
    @Singleton
    fun provideExceptionDatabase(@ApplicationContext context: Context): ExceptionDao {
        return Room.databaseBuilder(context, ExceptionDatabase::class.java, Constants.DATABASE_NAME)
            .build().exceptionDao()
    }

    @Provides
    @Singleton
    fun provideUserDatastore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(produceFile = {
            context.preferencesDataStoreFile(
                DATASTORE_NAME
            )
        })
    }
}